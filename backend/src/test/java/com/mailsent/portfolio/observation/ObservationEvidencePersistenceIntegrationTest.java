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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.application.DomainNotRegisteredException;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INF-002 observation-evidence persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V8 at context startup, and {@code ddl-auto: validate} verifies the
 * schema against the JPA entity. The DNS resolver is a deterministic
 * {@link TestConfiguration} double. Gated on {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ObservationEvidencePersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ObservationEvidenceService evidenceService;
    @Autowired private DataSource dataSource;
    @Autowired private SequenceResolver sequenceResolver;

    @TestConfiguration
    static class FakeResolverConfig {
        @Bean
        SequenceResolver sequenceResolver() {
            return new SequenceResolver();
        }

        @Bean
        @Primary
        DnsResolver fakeDnsResolver(SequenceResolver sequence) {
            return sequence;
        }
    }

    static final class SequenceResolver implements DnsResolver {
        private final AtomicInteger seq = new AtomicInteger();
        volatile List<MxRecord> mx = List.of(new MxRecord(10, "mail.example.com"));
        volatile List<String> txt = List.of("v=spf1 -all");
        volatile String spf = "v=spf1 -all";

        @Override
        public DnsObservation observe(String domainName) {
            Instant observedAt = BASE.plusSeconds(seq.getAndIncrement());
            return DnsObservation.of(domainName, mx, txt, spf, observedAt);
        }
    }

    private Domain registeredDomain() {
        String orgId = organizationService.create("Evidence " + UUID.randomUUID()).id();
        return domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
    }

    @Test
    void recordPersistReloadRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomain();
        ObservationEvidence created = evidenceService.record(domain.name());

        ObservationEvidence reloaded = evidenceService.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id());
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertEquals(List.of(new MxRecord(10, "mail.example.com")), reloaded.mxRecords());
        assertEquals(List.of("v=spf1 -all"), reloaded.txtRecords());
        assertEquals("v=spf1 -all", reloaded.spfRecord().orElseThrow());
        assertEquals(created.observedAt(), reloaded.observedAt());
        assertEquals(1, evidenceService.findByDomainId(domain.id()).size());
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> evidenceService.record("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        Domain domain = registeredDomain();
        sequenceResolver.mx = List.of(new MxRecord(10, "first.example.com"));
        ObservationEvidence first = evidenceService.record(domain.name());

        sequenceResolver.mx = List.of(new MxRecord(10, "second.example.com"));
        ObservationEvidence second = evidenceService.record(domain.name());

        assertNotEquals(first.id(), second.id());
        assertEquals(2, evidenceService.findByDomainId(domain.id()).size());
        ObservationEvidence reloadedFirst = evidenceService.findById(first.id()).orElseThrow();
        assertEquals("first.example.com", reloadedFirst.mxRecords().get(0).exchange());
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("second.example.com", second.mxRecords().get(0).exchange());
        sequenceResolver.mx = List.of(new MxRecord(10, "mail.example.com"));
    }

    @Test
    void emptyListsRoundTripAsEmptyLists() {
        Domain domain = registeredDomain();
        sequenceResolver.mx = List.of();
        sequenceResolver.txt = List.of();
        sequenceResolver.spf = null;

        ObservationEvidence saved = evidenceService.record(domain.name());
        ObservationEvidence reloaded = evidenceService.findById(saved.id()).orElseThrow();
        assertTrue(reloaded.mxRecords().isEmpty());
        assertTrue(reloaded.txtRecords().isEmpty());
        assertTrue(reloaded.spfRecord().isEmpty());

        sequenceResolver.mx = List.of(new MxRecord(10, "mail.example.com"));
        sequenceResolver.txt = List.of("v=spf1 -all");
        sequenceResolver.spf = "v=spf1 -all";
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        String orgA = organizationService.create("EvidenceA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("EvidenceB " + UUID.randomUUID()).id();
        Domain domainA = domainService.register(orgA, "it-" + UUID.randomUUID() + "a.com");
        Domain domainB = domainService.register(orgB, "it-" + UUID.randomUUID() + "b.com");

        ObservationEvidence forA = evidenceService.record(domainA.name());
        ObservationEvidence forB = evidenceService.record(domainB.name());

        List<ObservationEvidence> ownedByA = evidenceService.findByOrganizationId(orgA);
        List<ObservationEvidence> ownedByB = evidenceService.findByOrganizationId(orgB);
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
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "observation_evidence")) {
                while (pk.next()) {
                    if ("pk_observation_evidence".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_observation_evidence PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "observation_evidence")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_observation_evidence_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_observation_evidence_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_observation_evidence_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_observation_evidence_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "observation_evidence", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_observation_evidence_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_observation_evidence_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                        if (unique && "domain_id".equalsIgnoreCase(column)) {
                            uniqueOnDomainId = true;
                        }
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_observation_evidence".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_observation_evidence_organization_id is present");
            assertTrue(domainIdx, "idx_observation_evidence_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only snapshots)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "observation_evidence", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "observed_at",
                    "mx_records", "txt_records", "spf_record"), columns,
                "table stores only factual observation columns");
        }
    }
}
