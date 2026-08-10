package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * Real-PostgreSQL integration tests for the ORG-003 persistence path and invariants.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V5 at context startup, and {@code ddl-auto: validate} verifies the
 * schema against the JPA entities — so a failed entity/schema mismatch fails context
 * startup. These tests exercise the true path and the invariants the schema enforces:</p>
 *
 * <pre>
 * OrganizationService/DomainService → JPA adapters → Spring Data JPA → PostgreSQL → reload
 * </pre>
 *
 * <p>This test is <strong>not</strong> part of the hermetic default suite (the
 * {@code test} profile excludes the DataSource). It is gated on the system property
 * {@code mailsent.it.postgres} so it runs only when a live database is explicitly
 * supplied (ordinary {@code mvn verify} skips it; the CTO gates run it with
 * {@code -Dmailsent.it.postgres=true}).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DomainPersistenceIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DataSource dataSource;

    // A per-run prefix keeps every test repeatable against a shared, persistent schema
    // (domain names are UNIQUE globally).
    private static final String PREFIX = "it-" + java.util.UUID.randomUUID() + "-";

    private Organization createdOrganization() {
        return organizationService.create("Acme " + java.util.UUID.randomUUID());
    }

    @Test
    void registerPersistReloadRoundTripsThroughRealPostgres() {
        Organization org = createdOrganization();
        Domain created = domainService.register(org.id(), PREFIX + "roundtrip.com");

        Domain reloaded = domainService.findByDomain(PREFIX + "roundtrip.com").orElseThrow();
        assertEquals(created.id(), reloaded.id(), "stable identity unchanged");
        assertEquals(org.id(), reloaded.organizationId(), "owned by the granting organization");
        assertEquals(PREFIX + "roundtrip.com", reloaded.name());
        assertNotNull(reloaded.createdAt(), "registration timestamp persisted");
    }

    @Test
    void findByOrganizationIdReturnsOnlyThatOrganizationsDomains() {
        Organization orgA = createdOrganization();
        Organization orgB = createdOrganization();
        domainService.register(orgA.id(), PREFIX + "a.com");
        domainService.register(orgA.id(), PREFIX + "b.com");
        domainService.register(orgB.id(), PREFIX + "c.com");

        List<Domain> ownedByA = domainService.findByOrganizationId(orgA.id());
        List<Domain> ownedByB = domainService.findByOrganizationId(orgB.id());
        assertEquals(2, ownedByA.size(), "org A owns exactly its two domains");
        assertEquals(1, ownedByB.size(), "org B owns exactly its one domain");
        assertTrue(ownedByA.stream().allMatch(d -> d.organizationId().equals(orgA.id())),
            "ownership read never crosses an organization boundary (INV-09)");
    }

    @Test
    void duplicateNameIsRejected() {
        Organization org = createdOrganization();
        String name = PREFIX + "dup.com";
        domainService.register(org.id(), name);

        assertThrows(DomainAlreadyExistsException.class,
            () -> domainService.register(org.id(), name), "a duplicate name is rejected as a domain error");
    }

    @Test
    void registerUnderUnknownOrganizationIsRejected() {
        assertThrows(DomainOrganizationNotFoundException.class,
            () -> domainService.register("no-such-org", PREFIX + "orphan.com"),
            "registering under a non-existent organization yields a clear domain error");
    }

    @Test
    void concurrentSameNameRegistrationsProduceExactlyOneSuccessAndOneDomainError() throws Exception {
        Organization org = createdOrganization();
        String name = PREFIX + "race.com";

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Object> attempt = () -> {
                try {
                    return domainService.register(org.id(), name);
                } catch (Throwable t) {
                    return t;
                }
            };
            Future<Object> f1 = pool.submit(attempt);
            Future<Object> f2 = pool.submit(attempt);

            Object r1 = f1.get(30, TimeUnit.SECONDS);
            Object r2 = f2.get(30, TimeUnit.SECONDS);
            List<Object> results = List.of(r1, r2);

            long successes = results.stream().filter(Domain.class::isInstance).count();
            long duplicateErrors = results.stream()
                .filter(DomainAlreadyExistsException.class::isInstance).count();
            long leakedPersistence = results.stream()
                .filter(DataIntegrityViolationException.class::isInstance).count();

            assertEquals(1, successes, "exactly one concurrent registration succeeds");
            assertEquals(1, duplicateErrors, "the losing registration surfaces as DomainAlreadyExistsException");
            assertEquals(0, leakedPersistence, "no raw DataIntegrityViolationException may leak");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void schemaEnforcesGlobalUniqueNameAndOwnershipForeignKey() throws Exception {
        // ddl-auto: validate already failed startup if entity columns mismatched the
        // table; here we additionally prove the two constraints that validate does NOT
        // check — the UNIQUE name backstop and the ownership FK (INV-10 / INV-14).
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();

            boolean uniqueFound = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "domains", true, false)) {
                while (ri.next()) {
                    if ("uk_domains_name".equalsIgnoreCase(ri.getString("INDEX_NAME"))) {
                        uniqueFound = true;
                    }
                }
            }
            assertTrue(uniqueFound, "PostgreSQL enforces uk_domains_name UNIQUE(name)");

            boolean fkFound = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "domains")) {
                while (fk.next()) {
                    if ("fk_domains_organization".equalsIgnoreCase(fk.getString("FK_NAME"))
                            && "organization_id".equalsIgnoreCase(fk.getString("FKCOLUMN_NAME"))
                            && "organizations".equalsIgnoreCase(fk.getString("PKTABLE_NAME"))) {
                        fkFound = true;
                    }
                }
            }
            assertTrue(fkFound, "PostgreSQL enforces fk_domains_organization -> organizations(id)");
        }
    }
}
