package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
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
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcReportParseService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.application.DomainNotRegisteredException;
import com.mailsent.portfolio.observation.application.GooglePostmasterPort;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.application.MicrosoftSndsPort;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.application.ReputationProviderObservation;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the HEALTH-001 Health Signal
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V16 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. Gated on
 * {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class HealthSignalPersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-15T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ObservationEvidenceService evidenceService;
    @Autowired private ReputationObservationService reputationService;
    @Autowired private DmarcReportReceiveService receiveService;
    @Autowired private DmarcReportParseService parseService;
    @Autowired private DmarcAggregateEvidenceService aggregateService;
    @Autowired private HealthSignalService healthSignalService;
    @Autowired private DataSource dataSource;

    @TestConfiguration
    static class FakePortsConfig {
        @Bean
        SequenceResolver sequenceResolver() {
            return new SequenceResolver();
        }

        @Bean
        @Primary
        DnsResolver fakeDnsResolver(SequenceResolver sequence) {
            return sequence;
        }

        @Bean
        @Primary
        GooglePostmasterPort fakeGooglePostmasterPort() {
            return domainName -> Optional.of(
                new ReputationProviderObservation("it-postmaster", BASE));
        }

        @Bean
        @Primary
        MicrosoftSndsPort fakeMicrosoftSndsPort() {
            return domainName -> Optional.empty();
        }
    }

    static final class SequenceResolver implements DnsResolver {
        private final java.util.concurrent.atomic.AtomicInteger seq =
            new java.util.concurrent.atomic.AtomicInteger();

        @Override
        public DnsObservation observe(String domainName) {
            Instant observedAt = BASE.plusSeconds(seq.getAndIncrement());
            return DnsObservation.of(
                domainName,
                List.of(new MxRecord(10, "mail.example.com")),
                List.of("v=spf1 -all"),
                "v=spf1 -all",
                observedAt);
        }
    }

    private Domain registeredDomain() {
        String orgId = organizationService.create("Health " + UUID.randomUUID()).id();
        return domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
    }

    private void seedRequiredSources(Domain domain, String xml) {
        evidenceService.record(domain.name());
        reputationService.record(domain.name());
        var receipt = receiveService.receive(domain.id(), xml.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        aggregateService.store(parseService.parse(receipt.id()));
    }

    @Test
    void recordPersistReloadRoundTripsHealthyAtRiskAndUnknownThroughRealPostgres() {
        Domain healthyDomain = registeredDomain();
        seedRequiredSources(healthyDomain, xml(healthyDomain.name(), "none", "pass", "pass"));
        HealthSignal healthy = healthSignalService.record(healthyDomain.name()).orElseThrow();
        assertEquals(HealthIndication.HEALTHY, healthSignalService.findById(healthy.id()).orElseThrow().indication());
        ObservationEvidence oe = evidenceService.findByDomainId(healthyDomain.id()).get(0);
        assertEquals(oe.id(), healthy.observationEvidenceId());
        assertFalse(healthy.dmarcAggregateEvidenceIds().isEmpty());
        assertFalse(healthy.reputationObservationIds().isEmpty());
        assertEquals(healthyDomain.organizationId(), healthy.organizationId());
        assertEquals(1, healthSignalService.findByDomainId(healthyDomain.id()).size());

        Domain atRiskDomain = registeredDomain();
        seedRequiredSources(atRiskDomain, xml(atRiskDomain.name(), "none", "fail", "pass"));
        HealthSignal atRisk = healthSignalService.record(atRiskDomain.name()).orElseThrow();
        assertEquals(HealthIndication.AT_RISK, healthSignalService.findById(atRisk.id()).orElseThrow().indication());

        Domain unknownDomain = registeredDomain();
        seedRequiredSources(unknownDomain, xml(unknownDomain.name(), "none", "pass", "none"));
        HealthSignal unknown = healthSignalService.record(unknownDomain.name()).orElseThrow();
        assertEquals(HealthIndication.UNKNOWN, healthSignalService.findById(unknown.id()).orElseThrow().indication());
    }

    @Test
    void missingRequiredSourcePersistsNothing() {
        Domain domain = registeredDomain();
        evidenceService.record(domain.name());

        assertTrue(healthSignalService.record(domain.name()).isEmpty());
        assertTrue(healthSignalService.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> healthSignalService.record("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void idempotentWhenCitedIdsUnchangedAndAppendsWhenObservationEvidenceChanges() {
        Domain domain = registeredDomain();
        seedRequiredSources(domain, xml(domain.name(), "none", "pass", "pass"));
        HealthSignal first = healthSignalService.record(domain.name()).orElseThrow();

        assertTrue(healthSignalService.record(domain.name()).isEmpty());
        assertEquals(1, healthSignalService.findByDomainId(domain.id()).size());

        evidenceService.record(domain.name());
        HealthSignal second = healthSignalService.record(domain.name()).orElseThrow();

        assertNotEquals(first.id(), second.id());
        assertEquals(2, healthSignalService.findByDomainId(domain.id()).size());
        assertEquals(HealthIndication.HEALTHY, healthSignalService.findById(first.id()).orElseThrow().indication());
        assertEquals(HealthIndication.HEALTHY, second.indication());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        Domain domainA = registeredDomain();
        Domain domainB = registeredDomain();
        seedRequiredSources(domainA, xml(domainA.name(), "none", "pass", "pass"));
        seedRequiredSources(domainB, xml(domainB.name(), "quarantine", "pass", "pass"));

        HealthSignal forA = healthSignalService.record(domainA.name()).orElseThrow();
        HealthSignal forB = healthSignalService.record(domainB.name()).orElseThrow();

        List<HealthSignal> ownedByA = healthSignalService.findByOrganizationId(domainA.organizationId());
        List<HealthSignal> ownedByB = healthSignalService.findByOrganizationId(domainB.organizationId());
        assertEquals(HealthIndication.HEALTHY, forA.indication());
        assertEquals(HealthIndication.AT_RISK, forB.indication());
        assertTrue(ownedByA.stream().anyMatch(s -> s.id().equals(forA.id())));
        assertTrue(ownedByB.stream().anyMatch(s -> s.id().equals(forB.id())));
        assertTrue(ownedByA.stream().allMatch(s -> s.organizationId().equals(domainA.organizationId())));
        assertFalse(ownedByA.stream().anyMatch(s -> s.id().equals(forB.id())));
    }

    @Test
    void schemaEnforcesPrimaryKeyForeignKeysIndexesAndAllowsRepeatDomainSignals() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();

            boolean pkFound = false;
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "health_signal")) {
                while (pk.next()) {
                    if ("pk_health_signal".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_health_signal PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "health_signal")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_health_signal_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_health_signal_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_health_signal_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_health_signal_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "health_signal", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_health_signal_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_health_signal_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_health_signal".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_health_signal_organization_id is present");
            assertTrue(domainIdx, "idx_health_signal_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only snapshots)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "health_signal", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "indication",
                    "observation_evidence_id", "dmarc_aggregate_evidence_ids",
                    "reputation_observation_ids", "observed_at"), columns,
                "table stores only Health Signal envelope and lineage columns");
        }
    }

    private static String xml(String headerFrom, String disposition, String spf, String dkim) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <feedback>
              <report_metadata>
                <org_name>google.com</org_name>
                <report_id>it-health-%s</report_id>
                <date_range><begin>1609459200</begin><end>1609545599</end></date_range>
              </report_metadata>
              <policy_published><domain>other.example</domain><p>none</p></policy_published>
              <record>
                <row>
                  <source_ip>192.0.2.1</source_ip>
                  <count>1</count>
                  <policy_evaluated><disposition>%s</disposition><dkim>%s</dkim><spf>%s</spf></policy_evaluated>
                </row>
                <identifiers><header_from>%s</header_from></identifiers>
                <auth_results>
                  <dkim><domain>sel.example.com</domain><result>%s</result></dkim>
                  <spf><domain>%s</domain><result>%s</result></spf>
                </auth_results>
              </record>
            </feedback>
            """.formatted(UUID.randomUUID(), disposition, dkim, spf, headerFrom, dkim, headerFrom, spf);
    }
}
