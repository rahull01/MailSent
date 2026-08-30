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
import com.mailsent.identity.consent.application.ConsentRepository;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.DnsHealthObservationService;
import com.mailsent.portfolio.observation.application.DnsHealthObserver;
import com.mailsent.portfolio.observation.application.DnsObservationException;
import com.mailsent.portfolio.observation.application.DomainNotRegisteredException;
import com.mailsent.portfolio.observation.domain.DnsHealthFacts;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;
import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.DnssecPresence;
import com.mailsent.portfolio.observation.domain.PropagationAnswer;
import com.mailsent.portfolio.observation.domain.PropagationObservation;
import com.mailsent.portfolio.observation.domain.TtlObservation;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INF-007 DNS-health-observation
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V10 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. The observer is a deterministic
 * {@link TestConfiguration} double. Gated on {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DnsHealthObservationPersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ConsentRepository consentRepository;
    @Autowired private DnsHealthObservationService dnsHealthService;
    @Autowired private DataSource dataSource;
    @Autowired private SequenceObserver sequenceObserver;

    @TestConfiguration
    static class FakeObserverConfig {
        @Bean
        SequenceObserver sequenceObserver() {
            return new SequenceObserver();
        }

        @Bean
        @Primary
        DnsHealthObserver fakeDnsHealthObserver(SequenceObserver sequence) {
            return sequence;
        }
    }

    static final class SequenceObserver implements DnsHealthObserver {
        private final AtomicInteger seq = new AtomicInteger();
        volatile List<TtlObservation> ttl =
            List.of(new TtlObservation("MX", "example.com", "10 mail.example.com.", 3600));
        volatile List<DnssecObservation> dnssec =
            List.of(new DnssecObservation("DNSKEY", DnssecPresence.ABSENT, List.of()));
        volatile List<PropagationObservation> propagation = List.of(
            new PropagationObservation("system", "MX", "example.com", true,
                List.of(new PropagationAnswer("10 mail.example.com.", 3600))),
            PropagationObservation.unavailable("8.8.8.8", "MX", "example.com"));
        volatile boolean fail;

        @Override
        public DnsHealthFacts observe(String domainName) {
            if (fail) {
                throw new DnsObservationException("DNS health observation failed for " + domainName);
            }
            Instant observedAt = BASE.plusSeconds(seq.getAndIncrement());
            return DnsHealthFacts.of(observedAt, ttl, dnssec, propagation);
        }
    }

    private Domain registeredDomain() {
        String orgId = organizationService.create("DnsHealth " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
        // Production semantics: observation requires the owner's consent (INV-02).
        consentRepository.save(Consent.grant(orgId, domain.id(), "user-" + UUID.randomUUID(),
            ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
        return domain;
    }

    @Test
    void recordPersistReloadRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomain();
        DnsHealthObservation created = dnsHealthService.record(domain.name());

        DnsHealthObservation reloaded = dnsHealthService.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id());
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertEquals(created.ttlObservations(), reloaded.ttlObservations());
        assertEquals(created.dnssecObservations(), reloaded.dnssecObservations());
        assertEquals(created.propagationObservations(), reloaded.propagationObservations());
        assertEquals(3600L, reloaded.ttlObservations().get(0).ttlSeconds());
        assertEquals(DnssecPresence.ABSENT, reloaded.dnssecObservations().get(0).presence());
        assertTrue(reloaded.propagationObservations().get(0).available());
        assertFalse(reloaded.propagationObservations().get(1).available());
        assertTrue(reloaded.propagationObservations().get(1).answers().isEmpty());
        assertEquals(created.observedAt(), reloaded.observedAt());
        assertEquals(1, dnsHealthService.findByDomainId(domain.id()).size());
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> dnsHealthService.record("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        Domain domain = registeredDomain();
        sequenceObserver.ttl = List.of(new TtlObservation("MX", "example.com", "10 first.example.com.", 60));
        DnsHealthObservation first = dnsHealthService.record(domain.name());

        sequenceObserver.ttl = List.of(new TtlObservation("MX", "example.com", "10 second.example.com.", 120));
        DnsHealthObservation second = dnsHealthService.record(domain.name());

        assertNotEquals(first.id(), second.id());
        assertEquals(2, dnsHealthService.findByDomainId(domain.id()).size());
        DnsHealthObservation reloadedFirst = dnsHealthService.findById(first.id()).orElseThrow();
        assertEquals("10 first.example.com.", reloadedFirst.ttlObservations().get(0).rdata());
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("10 second.example.com.", second.ttlObservations().get(0).rdata());
        sequenceObserver.ttl = List.of(new TtlObservation("MX", "example.com", "10 mail.example.com.", 3600));
    }

    @Test
    void emptyListsRoundTripAsEmptyLists() {
        Domain domain = registeredDomain();
        sequenceObserver.ttl = List.of();
        sequenceObserver.dnssec = List.of();
        sequenceObserver.propagation = List.of(
            new PropagationObservation("system", "MX", "example.com", true, List.of()));

        DnsHealthObservation saved = dnsHealthService.record(domain.name());
        DnsHealthObservation reloaded = dnsHealthService.findById(saved.id()).orElseThrow();
        assertTrue(reloaded.ttlObservations().isEmpty());
        assertTrue(reloaded.dnssecObservations().isEmpty());
        assertTrue(reloaded.propagationObservations().get(0).answers().isEmpty());

        sequenceObserver.ttl = List.of(new TtlObservation("MX", "example.com", "10 mail.example.com.", 3600));
        sequenceObserver.dnssec = List.of(new DnssecObservation("DNSKEY", DnssecPresence.ABSENT, List.of()));
        sequenceObserver.propagation = List.of(
            new PropagationObservation("system", "MX", "example.com", true,
                List.of(new PropagationAnswer("10 mail.example.com.", 3600))),
            PropagationObservation.unavailable("8.8.8.8", "MX", "example.com"));
    }

    @Test
    void observationFailurePersistsNothing() {
        Domain domain = registeredDomain();
        sequenceObserver.fail = true;
        assertThrows(DnsObservationException.class, () -> dnsHealthService.record(domain.name()));
        assertTrue(dnsHealthService.findByDomainId(domain.id()).isEmpty());
        sequenceObserver.fail = false;
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        String orgA = organizationService.create("DnsHA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("DnsHB " + UUID.randomUUID()).id();
        Domain domainA = domainService.register(orgA, "it-" + UUID.randomUUID() + "a.com");
        Domain domainB = domainService.register(orgB, "it-" + UUID.randomUUID() + "b.com");
        consentRepository.save(Consent.grant(orgA, domainA.id(), "user-" + UUID.randomUUID(),
            ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
        consentRepository.save(Consent.grant(orgB, domainB.id(), "user-" + UUID.randomUUID(),
            ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));

        DnsHealthObservation forA = dnsHealthService.record(domainA.name());
        DnsHealthObservation forB = dnsHealthService.record(domainB.name());

        List<DnsHealthObservation> ownedByA = dnsHealthService.findByOrganizationId(orgA);
        List<DnsHealthObservation> ownedByB = dnsHealthService.findByOrganizationId(orgB);
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
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "dns_health_observation")) {
                while (pk.next()) {
                    if ("pk_dns_health_observation".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_dns_health_observation PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "dns_health_observation")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_dns_health_observation_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_dns_health_observation_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_dns_health_observation_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_dns_health_observation_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "dns_health_observation", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_dns_health_observation_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_dns_health_observation_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                        if (unique && "domain_id".equalsIgnoreCase(column)) {
                            uniqueOnDomainId = true;
                        }
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_dns_health_observation".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_dns_health_observation_organization_id is present");
            assertTrue(domainIdx, "idx_dns_health_observation_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only snapshots)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "dns_health_observation", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "observed_at",
                    "ttl_observations", "dnssec_observations", "propagation_observations"), columns,
                "table stores only factual observation columns");
        }
    }
}
