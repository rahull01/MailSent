package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;
import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemorySmtpGreetingObservationRepository;

/**
 * Unit tests for {@link SmtpGreetingObservationService} (INF-006) — registered
 * domain, tenant, MX→A/AAAA→SMTP, successful empty vs failure, append-only, and
 * no fabricated greeting or invented MX.
 */
class SmtpGreetingObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String GREETING = "220 mail.example.com ESMTP";
    private static final String EHLO = "250-PIPELINING\n250 HELP";

    private InMemoryDomainRepository domains;
    private InMemoryObservationEvidenceRepository evidence;
    private InMemorySmtpGreetingObservationRepository observations;
    private FakeForwardAddressLookup forward;
    private FakeSmtpProbe smtp;
    private SmtpGreetingObservationService service;
    private com.mailsent.identity.consent.application.ConsentService consentService;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        observations = new InMemorySmtpGreetingObservationRepository();
        forward = new FakeForwardAddressLookup();
        smtp = new FakeSmtpProbe();
        consentService = org.mockito.Mockito.mock(com.mailsent.identity.consent.application.ConsentService.class);
        service = new SmtpGreetingObservationService(observations, domains, evidence, forward, smtp, consentService);
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void recordPersistsIpv4GreetingForRegisteredDomain() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        smtp.succeed("192.0.2.10", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(domain.organizationId(), saved.get(0).organizationId());
        assertEquals(domain.id(), saved.get(0).domainId());
        assertEquals("example.com", saved.get(0).domainName());
        assertEquals("mail.example.com", saved.get(0).sourceMxHostname());
        assertEquals("192.0.2.10", saved.get(0).ip());
        assertEquals(IpAddressFamily.IPV4, saved.get(0).addressFamily());
        assertEquals(SmtpProbeOutcome.CONNECTED, saved.get(0).probeOutcome());
        assertEquals(GREETING, saved.get(0).rawGreeting());
        assertEquals(EHLO, saved.get(0).rawEhlo());
        assertTrue(observations.findById(saved.get(0).id()).isPresent());
    }

    @Test
    void recordPersistsIpv6Greeting() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV6, List.of("2001:db8::10"));
        smtp.succeed("2001:db8::10", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(IpAddressFamily.IPV6, saved.get(0).addressFamily());
        assertEquals("2001:db8::10", saved.get(0).ip());
    }

    @Test
    void recordRejectsUnregisteredDomain() {
        assertThrows(DomainNotRegisteredException.class, () -> service.record("not-registered.com"));
        assertTrue(observations.findByOrganizationId("org-1").isEmpty());
        assertEquals(0, forward.lookupCount);
        assertEquals(0, smtp.probeCount);
    }

    @Test
    void recordAcceptsOnlyDomainNameNotOrganizationId() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        smtp.succeed("192.0.2.10", GREETING, EHLO);

        assertTrue(Arrays.stream(SmtpGreetingObservationService.class.getMethods())
            .noneMatch(m -> "record".equals(m.getName()) && m.getParameterCount() > 1));

        SmtpGreetingObservation saved = service.record("  Example.COM  ").get(0);
        assertEquals("org-1", saved.organizationId());
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void noObservationEvidencePersistsNothingAndDoesNotInventMx() {
        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertTrue(saved.isEmpty());
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
        assertEquals(0, forward.lookupCount);
        assertEquals(0, smtp.probeCount);
    }

    @Test
    void emptyMxListPersistsNothing() {
        seedEvidence(List.of());

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertTrue(saved.isEmpty());
        assertEquals(0, forward.lookupCount);
        assertEquals(0, smtp.probeCount);
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
        smtp.succeed("192.0.2.20", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals("new.example.com", saved.get(0).sourceMxHostname());
        assertEquals("192.0.2.20", saved.get(0).ip());
    }

    @Test
    void multipleMxHostsPersistIndependentSnapshots() {
        seedEvidence(List.of(new MxRecord(10, "mx1.example.com"), new MxRecord(20, "mx2.example.com")));
        forward.succeed("mx1.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1"));
        forward.succeed("mx2.example.com", IpAddressFamily.IPV4, List.of("192.0.2.2"));
        smtp.succeed("192.0.2.1", GREETING, EHLO);
        smtp.succeed("192.0.2.2", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of("mx1.example.com", "mx2.example.com"),
            saved.stream().map(SmtpGreetingObservation::sourceMxHostname).collect(Collectors.toSet()));
    }

    @Test
    void multipleAddressesOnOneMxPersistOneSnapshotEach() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1", "192.0.2.2"));
        smtp.succeed("192.0.2.1", GREETING, EHLO);
        smtp.succeed("192.0.2.2", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of("192.0.2.1", "192.0.2.2"),
            saved.stream().map(SmtpGreetingObservation::ip).collect(Collectors.toSet()));
    }

    @Test
    void bothAddressFamiliesAreObservedWhenAddressesExist() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        forward.succeed("mail.example.com", IpAddressFamily.IPV6, List.of("2001:db8::10"));
        smtp.succeed("192.0.2.10", GREETING, EHLO);
        smtp.succeed("2001:db8::10", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of(IpAddressFamily.IPV4, IpAddressFamily.IPV6),
            saved.stream().map(SmtpGreetingObservation::addressFamily).collect(Collectors.toSet()));
    }

    @Test
    void unavailableAAndAaaaPersistNothingForThatFamily() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.unavailable("mail.example.com", IpAddressFamily.IPV4);
        forward.unavailable("mail.example.com", IpAddressFamily.IPV6);

        assertTrue(service.record("example.com").isEmpty());
        assertEquals(0, smtp.probeCount);
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void successfulEmptyADoesNotInventAddresses() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of());
        forward.succeed("mail.example.com", IpAddressFamily.IPV6, List.of());

        assertTrue(service.record("example.com").isEmpty());
        assertEquals(0, smtp.probeCount);
    }

    @Test
    void connectionRefusalTimeoutAndUnavailablePersistNothingWithoutFabricatedGreeting() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1", "192.0.2.2", "192.0.2.3"));
        smtp.refused("192.0.2.1");
        smtp.timeout("192.0.2.2");
        smtp.unavailable("192.0.2.3");

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void smtpProbeFailureIsUnavailableNotFabricatedGreeting() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        smtp.fail("192.0.2.10");

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void successfulEmptyGreetingAndEhloArePersisted() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        smtp.succeed("192.0.2.10", "", "");

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(SmtpProbeOutcome.CONNECTED, saved.get(0).probeOutcome());
        assertEquals("", saved.get(0).rawGreeting());
        assertEquals("", saved.get(0).rawEhlo());
    }

    @Test
    void oneFailedIpDoesNotBlockOtherSuccessfulIps() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.1", "192.0.2.2"));
        smtp.refused("192.0.2.1");
        smtp.succeed("192.0.2.2", GREETING, EHLO);

        List<SmtpGreetingObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals("192.0.2.2", saved.get(0).ip());
        assertEquals(GREETING, saved.get(0).rawGreeting());
        assertNull(observations.findByDomainId(domain.id()).stream()
            .filter(o -> "192.0.2.1".equals(o.ip()))
            .findFirst()
            .orElse(null));
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        smtp.succeed("192.0.2.10", "220 first", "250 first");
        SmtpGreetingObservation first = service.record("example.com").get(0);

        smtp.succeed("192.0.2.10", "220 second", "250 second");
        SmtpGreetingObservation second = service.record("example.com").get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, observations.findByDomainId(domain.id()).size());
        SmtpGreetingObservation reloadedFirst = observations.findById(first.id()).orElseThrow();
        assertEquals("220 first", reloadedFirst.rawGreeting());
        assertEquals("220 second", second.rawGreeting());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        seedEvidence(List.of(new MxRecord(10, "mail.example.com")));
        forward.succeed("mail.example.com", IpAddressFamily.IPV4, List.of("192.0.2.10"));
        smtp.succeed("192.0.2.10", GREETING, EHLO);

        Domain other = Domain.register("org-2", "other.com", NOW);
        domains.save(other);
        evidence.save(ObservationEvidence.capture(other, DnsObservation.of(
            "other.com", List.of(new MxRecord(10, "mx.other.com")), List.of(), null, NOW)));
        forward.succeed("mx.other.com", IpAddressFamily.IPV4, List.of("198.51.100.1"));
        smtp.succeed("198.51.100.1", GREETING, EHLO);

        SmtpGreetingObservation forOrg1 = service.record("example.com").get(0);
        SmtpGreetingObservation forOrg2 = service.record("other.com").get(0);

        List<SmtpGreetingObservation> org1 = service.findByOrganizationId("org-1");
        List<SmtpGreetingObservation> org2 = service.findByOrganizationId("org-2");
        assertEquals(1, org1.size());
        assertEquals(1, org2.size());
        assertEquals(forOrg1.id(), org1.get(0).id());
        assertEquals(forOrg2.id(), org2.get(0).id());
        assertEquals(1, service.findByDomainId(domain.id()).size());
        assertEquals(1, service.findByDomainId(other.id()).size());
    }

    @Test
    void repositoryPortHasNoUpdateOrDelete() {
        Set<String> names = Arrays.stream(SmtpGreetingObservationRepository.class.getDeclaredMethods())
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
        Class<?>[] params = SmtpGreetingObservationService.class.getConstructors()[0].getParameterTypes();
        assertEquals(SmtpGreetingObservationRepository.class, params[0]);
        assertEquals(com.mailsent.portfolio.domain.application.DomainRepository.class, params[1]);
        assertEquals(ObservationEvidenceRepository.class, params[2]);
        assertEquals(ForwardAddressLookup.class, params[3]);
        assertEquals(SmtpProbe.class, params[4]);
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Adapter")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Fake")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Socket")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("DnsJava")));
    }

    private void seedEvidence(List<MxRecord> mx) {
        evidence.save(ObservationEvidence.capture(domain, DnsObservation.of(
            "example.com", mx, List.of(), null, NOW)));
    }
}
