package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;

/**
 * Unit tests for {@link ObservationEvidenceService} (INF-002) — registration gate,
 * tenant derivation from Domain, factual persistence, append-only snapshots, and
 * DNS-failure atomicity.
 */
class ObservationEvidenceServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryObservationEvidenceRepository evidence;
    private ConfigurableResolver resolver;
    private ObservationEvidenceService service;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        resolver = new ConfigurableResolver();
        DnsObservationService dns = new DnsObservationService(resolver, domains, org.mockito.Mockito.mock(com.mailsent.identity.consent.application.ConsentService.class));
        service = new ObservationEvidenceService(evidence, domains, dns);
        domains.save(Domain.register("org-1", "example.com", NOW));
    }

    @Test
    void recordPersistsOneSnapshotForRegisteredDomain() {
        ObservationEvidence saved = service.record("example.com");

        assertEquals(1, evidence.findByDomainId(saved.domainId()).size());
        assertEquals("example.com", saved.domainName());
        assertTrue(evidence.findById(saved.id()).isPresent());
    }

    @Test
    void recordRejectsUnregisteredDomain() {
        assertThrows(DomainNotRegisteredException.class, () -> service.record("not-registered.com"));
        assertTrue(evidence.findByOrganizationId("org-1").isEmpty(), "nothing is persisted for an unregistered domain");
    }

    @Test
    void organizationIdComesFromRegisteredDomain() {
        ObservationEvidence saved = service.record("example.com");
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertEquals(domain.organizationId(), saved.organizationId());
        assertEquals("org-1", saved.organizationId());
    }

    @Test
    void recordAcceptsOnlyDomainNameNotOrganizationId() {
        boolean hasOrgIdWrite = Arrays.stream(ObservationEvidenceService.class.getDeclaredMethods())
            .filter(m -> "record".equals(m.getName()))
            .map(Method::getParameterTypes)
            .anyMatch(params -> Arrays.asList(params).contains(String.class) && params.length != 1);
        assertTrue(Arrays.stream(ObservationEvidenceService.class.getMethods())
            .noneMatch(m -> "record".equals(m.getName()) && m.getParameterCount() > 1));
        assertEquals(false, hasOrgIdWrite);

        ObservationEvidence saved = service.record("example.com");
        assertEquals("org-1", saved.organizationId(), "caller cannot select another organization");
    }

    @Test
    void persistedDomainIdentityMatchesRegisteredDomain() {
        Domain domain = domains.findByName("example.com").orElseThrow();
        ObservationEvidence saved = service.record("  Example.COM  ");
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void mxTxtSpfAndObservedAtMatchDnsObservation() {
        resolver.mx = List.of(new MxRecord(10, "mail.example.com"));
        resolver.txt = List.of("v=spf1 -all", "google-site-verification=abc");
        resolver.spf = "v=spf1 -all";
        resolver.observedAt = Instant.parse("2026-08-08T11:00:00Z");

        ObservationEvidence saved = service.record("example.com");

        assertEquals(resolver.mx, saved.mxRecords());
        assertEquals(List.of("google-site-verification=abc", "v=spf1 -all"), saved.txtRecords());
        assertEquals("v=spf1 -all", saved.spfRecord().orElseThrow());
        assertEquals(Instant.parse("2026-08-08T11:00:00Z"), saved.observedAt());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        resolver.mx = List.of(new MxRecord(10, "first.example.com"));
        resolver.observedAt = Instant.parse("2026-08-08T10:00:00Z");
        ObservationEvidence first = service.record("example.com");

        resolver.mx = List.of(new MxRecord(10, "second.example.com"));
        resolver.observedAt = Instant.parse("2026-08-08T11:00:00Z");
        ObservationEvidence second = service.record("example.com");

        assertNotEquals(first.id(), second.id());
        assertEquals(2, evidence.findByDomainId(first.domainId()).size());
        ObservationEvidence reloadedFirst = evidence.findById(first.id()).orElseThrow();
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("first.example.com", reloadedFirst.mxRecords().get(0).exchange());
        assertEquals("second.example.com", second.mxRecords().get(0).exchange());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        domains.save(Domain.register("org-2", "other.com", NOW));
        ObservationEvidence forOrg1 = service.record("example.com");
        ObservationEvidence forOrg2 = service.record("other.com");

        List<ObservationEvidence> org1 = service.findByOrganizationId("org-1");
        List<ObservationEvidence> org2 = service.findByOrganizationId("org-2");
        assertEquals(1, org1.size());
        assertEquals(1, org2.size());
        assertEquals(forOrg1.id(), org1.get(0).id());
        assertEquals(forOrg2.id(), org2.get(0).id());
        assertTrue(org1.stream().allMatch(e -> e.organizationId().equals("org-1")));
        assertTrue(org2.stream().allMatch(e -> e.organizationId().equals("org-2")));
    }

    @Test
    void dnsFailurePersistsNothing() {
        resolver.fail = true;
        assertThrows(DnsObservationException.class, () -> service.record("example.com"));
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(evidence.findByDomainId(domain.id()).isEmpty());
        assertTrue(evidence.findByOrganizationId("org-1").isEmpty());
    }

    private static final class ConfigurableResolver implements DnsResolver {
        List<MxRecord> mx = List.of();
        List<String> txt = List.of();
        String spf;
        Instant observedAt = NOW;
        boolean fail;
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public DnsObservation observe(String domainName) {
            calls.incrementAndGet();
            if (fail) {
                throw new DnsObservationException("DNS lookup failed for " + domainName);
            }
            return DnsObservation.of(domainName, mx, txt, spf, observedAt);
        }
    }
}
