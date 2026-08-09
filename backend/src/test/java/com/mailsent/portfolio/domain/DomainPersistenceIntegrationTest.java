package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * Real-PostgreSQL integration test for the ORG-003 persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V5 at context startup, and {@code ddl-auto: validate} verifies the
 * schema against the JPA entities — so this test exercises the true path:</p>
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

    @Test
    void registerPersistReloadThroughRealPostgres() {
        // A random suffix keeps this repeatable against a shared schema (name is UNIQUE),
        // mirroring how the org IT isolates per-run state.
        String name = "it-" + java.util.UUID.randomUUID() + ".com";

        Organization org = organizationService.create("Acme");
        Domain created = domainService.register(org.id(), name);

        Domain reloaded = domainService.findByDomain(name).orElseThrow();
        assertEquals(created.id(), reloaded.id(), "stable identity unchanged");
        assertEquals(org.id(), reloaded.organizationId(), "owned by the granting organization");
        assertEquals(name, reloaded.name());
        assertTrue(reloaded.createdAt() != null, "registration timestamp persisted");
    }
}