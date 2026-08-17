package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReverseDnsObservationRepository;

/**
 * Unit tests for {@link ReverseDnsObservationService} (INF-005) — registered
 * domain, tenant, MX→A/AAAA→PTR, unavailable vs empty, append-only, and no
 * fabricated PTR or invented MX.
 */
class ReverseDnsObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryObservationEvidenceRepository evidence;
    private InMemoryReverseDnsObservationRepository observations;
    private FakeForwardAddressLookup forward;
    private FakeReverseDnsLookup reverse;
    private ReverseDnsObservationService service;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        observations = new InMemoryReverseDnsObservationRepository();
        forward = new FakeForwardAddressLookup();
        reverse = new FakeReverseDnsLookup();
        service = new ReverseDnsObservationService(observations, domains, evidence, forward, reverse);
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void recordPersistsIpv4PtrForRegisteredDomain() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.succeed("192.0.2.10", List.of("ptr.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(domain.organizationId(), saved.get(0).organizationId());
        assertEquals(domain.id(), saved.get(0).domainId());
        assertEquals("example.com", saved.get(0).domainName());
        assertEquals("mail.example.com", saved.get(0).sourceMxHostname());
        assertEquals("192.0.2.10", saved.get(0).ip());
        assertEquals(IpAddressFamily.IPV4, saved.get(0).addressFamily());
        assertEquals(List.of("ptr.example.net"), saved.get(0).ptrValues());
        assertTrue(observations.findById(saved.get(0).id()).isPresent());
    }

    @Test
    void recordPersistsIpv6Ptr() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV6, List.of("2001:db8::10"));
        reverse.succeed("2001:db8::10", List.of("ptr6.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(IpAddressFamily.IPV6, saved.get(0).addressFamily());
        assertEquals("2001:db8::10", saved.get(0).ip());
        assertEquals(List.of("ptr6.example.net"), saved.get(0).ptrValues());
    }

    @Test
    void recordRejectsUnregisteredDomain() {
        assertThrows(DomainNotRegisteredException.class, () -> service.record("not-registered.com"));
        assertTrue(observations.findByOrganizationId("org-1").isEmpty());
        assertEquals(0, forward.lookupCount);
        assertEquals(0, reverse.lookupCount);
    }

    @Test
    void recordAcceptsOnlyDomainNameNotOrganizationId() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.succeed("192.0.2.10", List.of("ptr.example.net"));

        assertTrue(Arrays.stream(ReverseDnsObservationService.class.getMethods())
            .noneMatch(m -> "record".equals(m.getName()) && m.getParameterCount() > 1));

        ReverseDnsObservation saved = service.record("  Example.COM  ").get(0);
        assertEquals("org-1", saved.organizationId());
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void noObservationEvidencePersistsNothingAndDoesNotInventMx() {
        List<ReverseDnsObservation> saved = service.record("example.com");

        assertTrue(saved.isEmpty());
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
        assertEquals(0, forward.lookupCount);
        assertEquals(0, reverse.lookupCount);
    }

    @Test
    void emptyMxListPersistsNothing() {
        seedEvidence(List.of());

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertTrue(saved.isEmpty());
        assertEquals(0, forward.lookupCount);
        assertEquals(0, reverse.lookupCount);
    }

    @Test
    void usesLatestObservationEvidenceByObservedAt() {
        evidence.save(ObservationEvidence.capture(domain, DnsObservation.of(
            "example.com", List.of(new MxRecord(10, "old.example.com")), List.of(), null,
            Instant.parse("2026-08-08T09:00:00Z"))));
        evidence.save(ObservationEvidence.capture(domain, DnsObservation.of(
            "example.com", List.of(new MxRecord(10, "new.example.com")), List.of(), null,
            Instant.parse("2026-08-08T11:00:00Z"))));
        forward.succeed("new.example.com", IpAddressFamily.IPV4, List.of("192.0.2.20"));
        reverse.succeed("192.0.2.20", List.of("new-ptr.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals("new.example.com", saved.get(0).sourceMxHostname());
        assertEquals("192.0.2.20", saved.get(0).ip());
    }

    @Test
    void multipleMxHostsPersistIndependentSnapshots() {
        seedEvidence(List.of(new MxRecord(10, "mx1.example.com"), new MxRecord(20, "mx2.example.com")));
        forward.succeed("mx1.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1"));
        forward.succeed("mx2.example.com", IpAddressFamily.IPV4, List.of("192.0.2.2"));
        reverse.succeed("192.0.2.1", List.of("ptr1.example.net"));
        reverse.succeed("192.0.2.2", List.of("ptr2.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of("mx1.example.com", "mx2.example.com"),
            saved.stream().map(ReverseDnsObservation::sourceMxHostname).collect(Collectors.toSet()));
    }

    @Test
    void multipleAddressesOnOneMxPersistOneSnapshotEach() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1", "192.0.2.2"));
        reverse.succeed("192.0.2.1", List.of("a.example.net"));
        reverse.succeed("192.0.2.2", List.of("b.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of("192.0.2.1", "192.0.2.2"),
            saved.stream().map(ReverseDnsObservation::ip).collect(Collectors.toSet()));
    }

    @Test
    void multiplePtrValuesAreStoredAsObserved() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.succeed("192.0.2.10", List.of("one.example.net", "two.example.net"));

        ReverseDnsObservation saved = service.record("example.com").get(0);
        assertEquals(List.of("one.example.net", "two.example.net"), saved.ptrValues());
    }

    @Test
    void bothAddressFamiliesAreObservedWhenAddressesExist() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        forward.succeed("mail.example.com", IpAddressFamily.IPV6, List.of("2001:db8::10"));
        reverse.succeed("192.0.2.10", List.of("v4.example.net"));
        reverse.succeed("2001:db8::10", List.of("v6.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of(IpAddressFamily.IPV4, IpAddressFamily.IPV6),
            saved.stream().map(ReverseDnsObservation::addressFamily).collect(Collectors.toSet()));
    }

    @Test
    void unavailableAAndAaaaPersistNothingForThatFamily() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.unavailable("mail.example.com", IpAddressFamily.IPV4);
        forward.unavailable("mail.example.com", IpAddressFamily.IPV6);

        assertTrue(service.record("example.com").isEmpty());
        assertEquals(0, reverse.lookupCount);
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void successfulEmptyADoesNotInventAddresses() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of());
        forward.succeed("mail.example.com", IpAddressFamily.IPV6, List.of());

        assertTrue(service.record("example.com").isEmpty());
        assertEquals(0, reverse.lookupCount);
    }

    @Test
    void unavailablePtrPersistsNothingForThatIp() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.unavailable("192.0.2.10");

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void ptrLookupFailureIsUnavailableNotFabricatedEmptySuccess() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.fail("192.0.2.10");

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void successfulEmptyPtrListIsPersisted() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.succeed("192.0.2.10", List.of());

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertTrue(saved.get(0).ptrValues().isEmpty());
    }

    @Test
    void oneUnavailablePtrDoesNotBlockOtherSuccessfulIps() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1", "192.0.2.2"));
        reverse.unavailable("192.0.2.1");
        reverse.succeed("192.0.2.2", List.of("ok.example.net"));

        List<ReverseDnsObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals("192.0.2.2", saved.get(0).ip());
        assertEquals(List.of("ok.example.net"), saved.get(0).ptrValues());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.succeed("192.0.2.10", List.of("first.example.net"));
        ReverseDnsObservation first = service.record("example.com").get(0);

        reverse.succeed("192.0.2.10", List.of("second.example.net"));
        ReverseDnsObservation second = service.record("example.com").get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, observations.findByDomainId(domain.id()).size());
        ReverseDnsObservation reloadedFirst = observations.findById(first.id()).orElseThrow();
        assertEquals(List.of("first.example.net"), reloadedFirst.ptrValues());
        assertEquals(List.of("second.example.net"), second.ptrValues());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        reverse.succeed("192.0.2.10", List.of("ptr.example.net"));

        Domain other = Domain.register("org-2", "other.com", NOW);
        domains.save(other);
        evidence.save(ObservationEvidence.capture(other, DnsObservation.of(
            "other.com", List.of(new MxRecord(10, "mx.other.com")), List.of(), null, NOW)));
        forward.succeed("mx.other.com", IpAddressFamily.IPV4, List.of("198.51.100.1"));
        reverse.succeed("198.51.100.1", List.of("ptr.other.net"));

        ReverseDnsObservation forOrg1 = service.record("example.com").get(0);
        ReverseDnsObservation forOrg2 = service.record("other.com").get(0);

        List<ReverseDnsObservation> org1 = service.findByOrganizationId("org-1");
        List<ReverseDnsObservation> org2 = service.findByOrganizationId("org-2");
        assertEquals(1, org1.size());
        assertEquals(1, org2.size());
        assertEquals(forOrg1.id(), org1.get(0).id());
        assertEquals(forOrg2.id(), org2.get(0).id());
        assertEquals(1, service.findByDomainId(domain.id()).size());
        assertEquals(1, service.findByDomainId(other.id()).size());
    }

    @Test
    void repositoryPortHasNoUpdateOrDelete() {
        Set<String> names = Arrays.stream(ReverseDnsObservationRepository.class.getDeclaredMethods())
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
        Class<?>[] params = ReverseDnsObservationService.class.getConstructors()[0].getParameterTypes();
        assertEquals(ReverseDnsObservationRepository.class, params[0]);
        assertEquals(DomainRepository.class, params[1]);
        assertEquals(ObservationEvidenceRepository.class, params[2]);
        assertEquals(ForwardAddressLookup.class, params[3]);
        assertEquals(ReverseDnsLookup.class, params[4]);
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Adapter")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Fake")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("DnsJava")));
    }

    private void seedEvidence(List<MxRecord> mx) {
        evidence.save(ObservationEvidence.capture(domain, DnsObservation.of(
            "example.com", mx, List.of(), null, NOW)));
    }
}
