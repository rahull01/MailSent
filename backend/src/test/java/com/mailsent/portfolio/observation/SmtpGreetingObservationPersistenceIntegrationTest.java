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
import com.mailsent.portfolio.observation.application.ForwardAddressLookup;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.application.SmtpGreetingObservationService;
import com.mailsent.portfolio.observation.application.SmtpProbe;
import com.mailsent.portfolio.observation.application.SmtpProbeResult;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;
import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INF-006 SMTP-greeting-observation
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V13 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. DNS and SMTP ports are
 * deterministic {@link TestConfiguration} doubles. Gated on
 * {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class SmtpGreetingObservationPersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ObservationEvidenceRepository evidence;
    @Autowired private SmtpGreetingObservationService smtpGreetingService;
    @Autowired private DataSource dataSource;
    @Autowired private SequenceForward sequenceForward;
    @Autowired private SequenceSmtp sequenceSmtp;

    @TestConfiguration
    static class FakeProbeConfig {
        @Bean
        SequenceForward sequenceForward() {
            return new SequenceForward();
        }

        @Bean
        SequenceSmtp sequenceSmtp() {
            return new SequenceSmtp();
        }

        @Bean
        @Primary
        ForwardAddressLookup fakeForwardAddressLookup(SequenceForward sequence) {
            return sequence;
        }

        @Bean
        @Primary
        SmtpProbe fakeSmtpProbe(SequenceSmtp sequence) {
            return sequence;
        }
    }

    static final class SequenceForward implements ForwardAddressLookup {
        volatile boolean ipv4Available = true;
        volatile boolean ipv6Available = false;
        volatile List<String> ipv4 = List.of("192.0.2.10");
        volatile List<String> ipv6 = List.of();

        @Override
        public Optional<List<String>> lookup(String hostname, IpAddressFamily family) {
            if (family == IpAddressFamily.IPV4) {
                return ipv4Available ? Optional.of(ipv4) : Optional.empty();
            }
            return ipv6Available ? Optional.of(ipv6) : Optional.empty();
        }
    }

    static final class SequenceSmtp implements SmtpProbe {
        volatile SmtpProbeResult result = SmtpProbeResult.connected("220 mail ESMTP", "250 HELP");

        @Override
        public SmtpProbeResult probe(String ip, String ehloDomain) {
            return result;
        }
    }

    private Domain registeredDomainWithMx() {
        String orgId = organizationService.create("SMTP " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
        evidence.save(ObservationEvidence.capture(domain, DnsObservation.of(
            domain.name(), List.of(new MxRecord(10, "mail." + domain.name())), List.of(), null, BASE)));
        return domain;
    }

    @Test
    void recordPersistReloadRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomainWithMx();
        SmtpGreetingObservation created = smtpGreetingService.record(domain.name()).get(0);

        SmtpGreetingObservation reloaded = smtpGreetingService.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id());
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertEquals("mail." + domain.name(), reloaded.sourceMxHostname());
        assertEquals("192.0.2.10", reloaded.ip());
        assertEquals(IpAddressFamily.IPV4, reloaded.addressFamily());
        assertEquals(SmtpProbeOutcome.CONNECTED, reloaded.probeOutcome());
        assertEquals("220 mail ESMTP", reloaded.rawGreeting());
        assertEquals("250 HELP", reloaded.rawEhlo());
        assertEquals(created.observedAt(), reloaded.observedAt());
        assertEquals(1, smtpGreetingService.findByDomainId(domain.id()).size());
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> smtpGreetingService.record("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void noObservationEvidencePersistsNothing() {
        String orgId = organizationService.create("SMTP-empty " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");

        assertTrue(smtpGreetingService.record(domain.name()).isEmpty());
        assertTrue(smtpGreetingService.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        Domain domain = registeredDomainWithMx();
        sequenceSmtp.result = SmtpProbeResult.connected("220 first", "250 first");
        SmtpGreetingObservation first = smtpGreetingService.record(domain.name()).get(0);

        sequenceSmtp.result = SmtpProbeResult.connected("220 second", "250 second");
        SmtpGreetingObservation second = smtpGreetingService.record(domain.name()).get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, smtpGreetingService.findByDomainId(domain.id()).size());
        SmtpGreetingObservation reloadedFirst = smtpGreetingService.findById(first.id()).orElseThrow();
        assertEquals("220 first", reloadedFirst.rawGreeting());
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("220 second", second.rawGreeting());
        sequenceSmtp.result = SmtpProbeResult.connected("220 mail ESMTP", "250 HELP");
    }

    @Test
    void unavailableProbePersistsNothing() {
        Domain domain = registeredDomainWithMx();
        sequenceSmtp.result = SmtpProbeResult.unavailable();

        assertTrue(smtpGreetingService.record(domain.name()).isEmpty());
        assertTrue(smtpGreetingService.findByDomainId(domain.id()).isEmpty());

        sequenceSmtp.result = SmtpProbeResult.connected("220 mail ESMTP", "250 HELP");
    }

    @Test
    void successfulEmptyGreetingPersists() {
        Domain domain = registeredDomainWithMx();
        sequenceSmtp.result = SmtpProbeResult.connected("", "");

        SmtpGreetingObservation saved = smtpGreetingService.record(domain.name()).get(0);
        assertEquals("", saved.rawGreeting());
        assertEquals("", saved.rawEhlo());
        sequenceSmtp.result = SmtpProbeResult.connected("220 mail ESMTP", "250 HELP");
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        String orgA = organizationService.create("SmtpA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("SmtpB " + UUID.randomUUID()).id();
        Domain domainA = domainService.register(orgA, "it-" + UUID.randomUUID() + "a.com");
        Domain domainB = domainService.register(orgB, "it-" + UUID.randomUUID() + "b.com");
        evidence.save(ObservationEvidence.capture(domainA, DnsObservation.of(
            domainA.name(), List.of(new MxRecord(10, "mail." + domainA.name())), List.of(), null, BASE)));
        evidence.save(ObservationEvidence.capture(domainB, DnsObservation.of(
            domainB.name(), List.of(new MxRecord(10, "mail." + domainB.name())), List.of(), null, BASE)));

        SmtpGreetingObservation forA = smtpGreetingService.record(domainA.name()).get(0);
        SmtpGreetingObservation forB = smtpGreetingService.record(domainB.name()).get(0);

        List<SmtpGreetingObservation> ownedByA = smtpGreetingService.findByOrganizationId(orgA);
        List<SmtpGreetingObservation> ownedByB = smtpGreetingService.findByOrganizationId(orgB);
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
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "smtp_greeting_observation")) {
                while (pk.next()) {
                    if ("pk_smtp_greeting_observation".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_smtp_greeting_observation PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "smtp_greeting_observation")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_smtp_greeting_observation_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_smtp_greeting_observation_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_smtp_greeting_observation_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_smtp_greeting_observation_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "smtp_greeting_observation", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_smtp_greeting_observation_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_smtp_greeting_observation_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_smtp_greeting_observation".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_smtp_greeting_observation_organization_id is present");
            assertTrue(domainIdx, "idx_smtp_greeting_observation_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only snapshots)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "smtp_greeting_observation", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "source_mx_hostname",
                    "ip", "address_family", "probe_outcome", "raw_greeting", "raw_ehlo", "observed_at"), columns,
                "table stores only factual SMTP greeting observation columns");
        }
    }
}
