package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;
import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.DnssecPresence;
import com.mailsent.portfolio.observation.domain.PropagationAnswer;
import com.mailsent.portfolio.observation.domain.PropagationObservation;
import com.mailsent.portfolio.observation.domain.TtlObservation;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDnsHealthObservationRepository;

/**
 * Unit tests for {@link DnsHealthObservationService} (INF-007) — registration
 * gate, tenant derivation from Domain, factual persistence, append-only
 * snapshots, and observation-failure atomicity.
 */
class DnsHealthObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryDnsHealthObservationRepository observations;
    private FakeDnsHealthObserver observer;
    private DnsHealthObservationService service;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        observations = new InMemoryDnsHealthObservationRepository();
        observer = new FakeDnsHealthObserver();
        service = new DnsHealthObservationService(observations, domains, observer);
        domains.save(Domain.register("org-1", "example.com", NOW));
    }

    @Test
    void recordPersistsOneSnapshotForRegisteredDomain() {
        DnsHealthObservation saved = service.record("example.com");

        assertEquals(1, observations.findByDomainId(saved.domainId()).size());
        assertEquals("example.com", saved.domainName());
        assertTrue(observations.findById(saved.id()).isPresent());
    }

    @Test
    void recordRejectsUnregisteredDomain() {
        assertThrows(DomainNotRegisteredException.class, () -> service.record("not-registered.com"));
        assertTrue(observations.findByOrganizationId("org-1").isEmpty(),
            "nothing is persisted for an unregistered domain");
    }

    @Test
    void organizationIdAndDomainIdComeFromRegisteredDomain() {
        DnsHealthObservation saved = service.record("example.com");
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertEquals(domain.organizationId(), saved.organizationId());
        assertEquals("org-1", saved.organizationId());
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void recordAcceptsOnlyDomainNameNotOrganizationId() {
        boolean hasOrgIdWrite = Arrays.stream(DnsHealthObservationService.class.getDeclaredMethods())
            .filter(m -> "record".equals(m.getName()))
            .map(Method::getParameterTypes)
            .anyMatch(params -> Arrays.asList(params).contains(String.class) && params.length != 1);
        assertTrue(Arrays.stream(DnsHealthObservationService.class.getMethods())
            .noneMatch(m -> "record".equals(m.getName()) && m.getParameterCount() > 1));
        assertEquals(false, hasOrgIdWrite);

        DnsHealthObservation saved = service.record("example.com");
        assertEquals("org-1", saved.organizationId(), "caller cannot select another organization");
    }

    @Test
    void persistedDomainIdentityMatchesRegisteredDomain() {
        Domain domain = domains.findByName("example.com").orElseThrow();
        DnsHealthObservation saved = service.record("  Example.COM  ");
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void ttlDnssecAndPropagationFactsArePreserved() {
        Instant observedAt = Instant.parse("2026-08-08T11:00:00Z");
        observer.observedAt = observedAt;
        observer.ttl = List.of(new TtlObservation("TXT", "example.com", "\"v=spf1 -all\"", 300));
        observer.dnssec = List.of(new DnssecObservation("DNSKEY", DnssecPresence.ABSENT, List.of()));
        observer.propagation = List.of(
            new PropagationObservation("system", "MX", "example.com", true,
                List.of(new PropagationAnswer("10 mail.example.com.", 3600))),
            PropagationObservation.unavailable("8.8.8.8", "MX", "example.com"));

        DnsHealthObservation saved = service.record("example.com");

        assertEquals(observer.ttl, saved.ttlObservations());
        assertEquals(observer.dnssec, saved.dnssecObservations());
        assertEquals(observer.propagation, saved.propagationObservations());
        assertEquals(observedAt, saved.observedAt());
        assertEquals(300L, saved.ttlObservations().get(0).ttlSeconds());
        assertEquals(DnssecPresence.ABSENT, saved.dnssecObservations().get(0).presence());
        assertTrue(saved.propagationObservations().get(0).available());
        assertFalse(saved.propagationObservations().get(1).available());
        assertTrue(saved.propagationObservations().get(1).answers().isEmpty());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        observer.ttl = List.of(new TtlObservation("MX", "example.com", "10 first.example.com.", 60));
        observer.observedAt = Instant.parse("2026-08-08T10:00:00Z");
        DnsHealthObservation first = service.record("example.com");

        observer.ttl = List.of(new TtlObservation("MX", "example.com", "10 second.example.com.", 120));
        observer.observedAt = Instant.parse("2026-08-08T11:00:00Z");
        DnsHealthObservation second = service.record("example.com");

        assertNotEquals(first.id(), second.id());
        assertEquals(2, observations.findByDomainId(first.domainId()).size());
        DnsHealthObservation reloadedFirst = observations.findById(first.id()).orElseThrow();
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("10 first.example.com.", reloadedFirst.ttlObservations().get(0).rdata());
        assertEquals("10 second.example.com.", second.ttlObservations().get(0).rdata());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        domains.save(Domain.register("org-2", "other.com", NOW));
        DnsHealthObservation forOrg1 = service.record("example.com");
        DnsHealthObservation forOrg2 = service.record("other.com");

        List<DnsHealthObservation> org1 = service.findByOrganizationId("org-1");
        List<DnsHealthObservation> org2 = service.findByOrganizationId("org-2");
        assertEquals(1, org1.size());
        assertEquals(1, org2.size());
        assertEquals(forOrg1.id(), org1.get(0).id());
        assertEquals(forOrg2.id(), org2.get(0).id());
        assertTrue(org1.stream().allMatch(e -> e.organizationId().equals("org-1")));
        assertTrue(org2.stream().allMatch(e -> e.organizationId().equals("org-2")));
        assertEquals(1, service.findByDomainId(forOrg1.domainId()).size());
        assertEquals(1, service.findByDomainId(forOrg2.domainId()).size());
    }

    @Test
    void observationFailurePersistsNothing() {
        observer.fail = true;
        assertThrows(DnsObservationException.class, () -> service.record("example.com"));
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
        assertTrue(observations.findByOrganizationId("org-1").isEmpty());
    }

    @Test
    void repositoryPortHasNoUpdateOrDelete() {
        Set<String> names = Arrays.stream(DnsHealthObservationRepository.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.contains("save"));
        assertTrue(names.contains("findById"));
        assertTrue(names.contains("findByDomainId"));
        assertTrue(names.contains("findByOrganizationId"));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("update")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("delete")));
    }

    @Test
    void serviceDependsOnPortsNotConcreteAdapters() {
        Class<?>[] params = DnsHealthObservationService.class.getConstructors()[0].getParameterTypes();
        assertEquals(DnsHealthObservationRepository.class, params[0]);
        assertEquals(DomainRepository.class, params[1]);
        assertEquals(DnsHealthObserver.class, params[2]);
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Adapter")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Fake")));
    }
}
