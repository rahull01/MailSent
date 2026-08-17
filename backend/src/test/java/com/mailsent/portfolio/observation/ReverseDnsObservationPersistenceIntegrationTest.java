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
import com.mailsent.portfolio.observation.application.ReverseDnsLookup;
import com.mailsent.portfolio.observation.application.ReverseDnsObservationService;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INF-005 reverse-DNS-observation
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V12 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. DNS ports are deterministic
 * {@link TestConfiguration} doubles. Gated on {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ReverseDnsObservationPersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ObservationEvidenceRepository evidence;
    @Autowired private ReverseDnsObservationService reverseDnsService;
    @Autowired private DataSource dataSource;
    @Autowired private SequenceForward sequenceForward;
    @Autowired private SequenceReverse sequenceReverse;

    @TestConfiguration
    static class FakeDnsConfig {
        @Bean
        SequenceForward sequenceForward() {
            return new SequenceForward();
        }

        @Bean
        SequenceReverse sequenceReverse() {
            return new SequenceReverse();
        }

        @Bean
        @Primary
        ForwardAddressLookup fakeForwardAddressLookup(SequenceForward sequence) {
            return sequence;
        }

        @Bean
        @Primary
        ReverseDnsLookup fakeReverseDnsLookup(SequenceReverse sequence) {
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

    static final class SequenceReverse implements ReverseDnsLookup {
        volatile boolean available = true;
        volatile List<String> ptrValues = List.of("ptr.example.net");

        @Override
        public Optional<List<String>> lookupPtr(String ipAddress) {
            if (!available) {
                return Optional.empty();
            }
            return Optional.of(ptrValues);
        }
    }

    private Domain registeredDomainWithMx() {
        String orgId = organizationService.create("RDNS " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
        evidence.save(ObservationEvidence.capture(domain, DnsObservation.of(
            domain.name(), List.of(new MxRecord(10, "mail." + domain.name())), List.of(), null, BASE)));
        return domain;
    }

    @Test
    void recordPersistReloadRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomainWithMx();
        ReverseDnsObservation created = reverseDnsService.record(domain.name()).get(0);

        ReverseDnsObservation reloaded = reverseDnsService.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id());
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertEquals("mail." + domain.name(), reloaded.sourceMxHostname());
        assertEquals("192.0.2.10", reloaded.ip());
        assertEquals(IpAddressFamily.IPV4, reloaded.addressFamily());
        assertEquals(List.of("ptr.example.net"), reloaded.ptrValues());
        assertEquals(created.observedAt(), reloaded.observedAt());
        assertEquals(1, reverseDnsService.findByDomainId(domain.id()).size());
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> reverseDnsService.record("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void noObservationEvidencePersistsNothing() {
        String orgId = organizationService.create("RDNS-empty " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");

        assertTrue(reverseDnsService.record(domain.name()).isEmpty());
        assertTrue(reverseDnsService.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        Domain domain = registeredDomainWithMx();
        sequenceReverse.ptrValues = List.of("first.example.net");
        ReverseDnsObservation first = reverseDnsService.record(domain.name()).get(0);

        sequenceReverse.ptrValues = List.of("second.example.net");
        ReverseDnsObservation second = reverseDnsService.record(domain.name()).get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, reverseDnsService.findByDomainId(domain.id()).size());
        ReverseDnsObservation reloadedFirst = reverseDnsService.findById(first.id()).orElseThrow();
        assertEquals(List.of("first.example.net"), reloadedFirst.ptrValues());
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals(List.of("second.example.net"), second.ptrValues());
        sequenceReverse.ptrValues = List.of("ptr.example.net");
    }

    @Test
    void unavailablePtrPersistsNothing() {
        Domain domain = registeredDomainWithMx();
        sequenceReverse.available = false;

        assertTrue(reverseDnsService.record(domain.name()).isEmpty());
        assertTrue(reverseDnsService.findByDomainId(domain.id()).isEmpty());

        sequenceReverse.available = true;
    }

    @Test
    void successfulEmptyPtrListPersists() {
        Domain domain = registeredDomainWithMx();
        sequenceReverse.ptrValues = List.of();

        ReverseDnsObservation saved = reverseDnsService.record(domain.name()).get(0);
        assertTrue(saved.ptrValues().isEmpty());
        sequenceReverse.ptrValues = List.of("ptr.example.net");
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        String orgA = organizationService.create("RdnsA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("RdnsB " + UUID.randomUUID()).id();
        Domain domainA = domainService.register(orgA, "it-" + UUID.randomUUID() + "a.com");
        Domain domainB = domainService.register(orgB, "it-" + UUID.randomUUID() + "b.com");
        evidence.save(ObservationEvidence.capture(domainA, DnsObservation.of(
            domainA.name(), List.of(new MxRecord(10, "mail." + domainA.name())), List.of(), null, BASE)));
        evidence.save(ObservationEvidence.capture(domainB, DnsObservation.of(
            domainB.name(), List.of(new MxRecord(10, "mail." + domainB.name())), List.of(), null, BASE)));

        ReverseDnsObservation forA = reverseDnsService.record(domainA.name()).get(0);
        ReverseDnsObservation forB = reverseDnsService.record(domainB.name()).get(0);

        List<ReverseDnsObservation> ownedByA = reverseDnsService.findByOrganizationId(orgA);
        List<ReverseDnsObservation> ownedByB = reverseDnsService.findByOrganizationId(orgB);
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
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "reverse_dns_observation")) {
                while (pk.next()) {
                    if ("pk_reverse_dns_observation".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_reverse_dns_observation PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "reverse_dns_observation")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_reverse_dns_observation_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_reverse_dns_observation_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_reverse_dns_observation_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_reverse_dns_observation_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "reverse_dns_observation", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_reverse_dns_observation_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_reverse_dns_observation_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_reverse_dns_observation".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_reverse_dns_observation_organization_id is present");
            assertTrue(domainIdx, "idx_reverse_dns_observation_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only snapshots)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "reverse_dns_observation", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "source_mx_hostname",
                    "ip", "address_family", "ptr_values", "observed_at"), columns,
                "table stores only factual reverse-DNS observation columns");
        }
    }
}
