package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.DomainNotRegisteredException;
import com.mailsent.portfolio.observation.application.GooglePostmasterPort;
import com.mailsent.portfolio.observation.application.MicrosoftSndsPort;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.application.ReputationProviderObservation;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INF-003 reputation-observation
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V9 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. Provider ports are deterministic
 * {@link TestConfiguration} doubles. Gated on {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ReputationObservationPersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ReputationObservationService reputationService;
    @Autowired private DataSource dataSource;
    @Autowired private SequencePostmaster sequencePostmaster;
    @Autowired private SequenceSnds sequenceSnds;

    @TestConfiguration
    static class FakeProviderConfig {
        @Bean
        SequencePostmaster sequencePostmaster() {
            return new SequencePostmaster();
        }

        @Bean
        SequenceSnds sequenceSnds() {
            return new SequenceSnds();
        }

        @Bean
        @Primary
        GooglePostmasterPort fakeGooglePostmasterPort(SequencePostmaster sequence) {
            return sequence;
        }

        @Bean
        @Primary
        MicrosoftSndsPort fakeMicrosoftSndsPort(SequenceSnds sequence) {
            return sequence;
        }
    }

    static final class SequencePostmaster implements GooglePostmasterPort {
        volatile boolean available = true;
        volatile String payload = "it-postmaster";
        volatile Instant observedAt = BASE;

        @Override
        public Optional<ReputationProviderObservation> query(String domainName) {
            if (!available) {
                return Optional.empty();
            }
            return Optional.of(new ReputationProviderObservation(payload, observedAt));
        }
    }

    static final class SequenceSnds implements MicrosoftSndsPort {
        volatile boolean available = false;
        volatile String payload = "it-snds";
        volatile Instant observedAt = BASE;

        @Override
        public Optional<ReputationProviderObservation> query(String domainName) {
            if (!available) {
                return Optional.empty();
            }
            return Optional.of(new ReputationProviderObservation(payload, observedAt));
        }
    }

    private Domain registeredDomain() {
        String orgId = organizationService.create("Reputation " + UUID.randomUUID()).id();
        return domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
    }

    @Test
    void recordPersistReloadRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomain();
        ReputationObservation created = reputationService.record(domain.name()).get(0);

        ReputationObservation reloaded = reputationService.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id());
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertEquals(ReputationSource.POSTMASTER, reloaded.source());
        assertEquals("it-postmaster", reloaded.payload());
        assertEquals(created.observedAt(), reloaded.observedAt());
        assertEquals(1, reputationService.findByDomainId(domain.id()).size());
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> reputationService.record("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        Domain domain = registeredDomain();
        sequencePostmaster.payload = "first";
        ReputationObservation first = reputationService.record(domain.name()).get(0);

        sequencePostmaster.payload = "second";
        ReputationObservation second = reputationService.record(domain.name()).get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, reputationService.findByDomainId(domain.id()).size());
        ReputationObservation reloadedFirst = reputationService.findById(first.id()).orElseThrow();
        assertEquals("first", reloadedFirst.payload());
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("second", second.payload());
        sequencePostmaster.payload = "it-postmaster";
    }

    @Test
    void unavailableProviderPersistsNothingForThatSource() {
        Domain domain = registeredDomain();
        sequencePostmaster.available = false;
        sequenceSnds.available = false;

        assertTrue(reputationService.record(domain.name()).isEmpty());
        assertTrue(reputationService.findByDomainId(domain.id()).isEmpty());

        sequencePostmaster.available = true;
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        String orgA = organizationService.create("RepA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("RepB " + UUID.randomUUID()).id();
        Domain domainA = domainService.register(orgA, "it-" + UUID.randomUUID() + "a.com");
        Domain domainB = domainService.register(orgB, "it-" + UUID.randomUUID() + "b.com");

        ReputationObservation forA = reputationService.record(domainA.name()).get(0);
        ReputationObservation forB = reputationService.record(domainB.name()).get(0);

        List<ReputationObservation> ownedByA = reputationService.findByOrganizationId(orgA);
        List<ReputationObservation> ownedByB = reputationService.findByOrganizationId(orgB);
        assertTrue(ownedByA.stream().anyMatch(e -> e.id().equals(forA.id())));
        assertTrue(ownedByB.stream().anyMatch(e -> e.id().equals(forB.id())));
        assertTrue(ownedByA.stream().allMatch(e -> e.organizationId().equals(orgA)));
        assertTrue(ownedByB.stream().allMatch(e -> e.organizationId().equals(orgB)));
        assertFalse(ownedByA.stream().anyMatch(e -> e.id().equals(forB.id())));
    }

    @Test
    void schemaEnforcesPrimaryKeyForeignKeysIndexesAndAllowsRepeatDomainObservations() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();

            boolean pkFound = false;
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "reputation_observation")) {
                while (pk.next()) {
                    if ("pk_reputation_observation".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_reputation_observation PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "reputation_observation")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_reputation_observation_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_reputation_observation_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_reputation_observation_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_reputation_observation_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "reputation_observation", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_reputation_observation_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_reputation_observation_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                        if (unique && "domain_id".equalsIgnoreCase(column)) {
                            uniqueOnDomainId = true;
                        }
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_reputation_observation".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_reputation_observation_organization_id is present");
            assertTrue(domainIdx, "idx_reputation_observation_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only snapshots)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "reputation_observation", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "source",
                    "observed_at", "payload"), columns,
                "table stores only factual observation columns");
        }
    }
}
