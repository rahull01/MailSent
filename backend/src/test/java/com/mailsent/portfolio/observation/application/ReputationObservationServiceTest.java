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
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReputationObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.UnavailableGooglePostmasterAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableMicrosoftSndsAdapter;

/**
 * Unit tests for {@link ReputationObservationService} (INF-003) — registration
 * gate, tenant derivation from Domain, factual persistence, append-only
 * snapshots, and provider-failure atomicity.
 */
class ReputationObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryReputationObservationRepository observations;
    private FakeGooglePostmasterPort postmaster;
    private FakeMicrosoftSndsPort snds;
    private ReputationObservationService service;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        observations = new InMemoryReputationObservationRepository();
        postmaster = new FakeGooglePostmasterPort();
        snds = new FakeMicrosoftSndsPort();
        service = new ReputationObservationService(observations, domains, postmaster, snds);
        domains.save(Domain.register("org-1", "example.com", NOW));
        postmaster.available();
        snds.unavailable();
    }

    @Test
    void recordPersistsOneSnapshotForRegisteredDomainWhenOneProviderReturnsAFact() {
        List<ReputationObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals("example.com", saved.get(0).domainName());
        assertTrue(observations.findById(saved.get(0).id()).isPresent());
    }

    @Test
    void recordRejectsUnregisteredDomain() {
        assertThrows(DomainNotRegisteredException.class, () -> service.record("not-registered.com"));
        assertTrue(observations.findByOrganizationId("org-1").isEmpty(),
            "nothing is persisted for an unregistered domain");
    }

    @Test
    void organizationIdComesFromRegisteredDomain() {
        ReputationObservation saved = service.record("example.com").get(0);
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertEquals(domain.organizationId(), saved.organizationId());
        assertEquals("org-1", saved.organizationId());
        assertEquals(domain.id(), saved.domainId());
    }

    @Test
    void recordAcceptsOnlyDomainNameNotOrganizationId() {
        boolean hasOrgIdWrite = Arrays.stream(ReputationObservationService.class.getDeclaredMethods())
            .filter(m -> "record".equals(m.getName()))
            .map(Method::getParameterTypes)
            .anyMatch(params -> Arrays.asList(params).contains(String.class) && params.length != 1);
        assertTrue(Arrays.stream(ReputationObservationService.class.getMethods())
            .noneMatch(m -> "record".equals(m.getName()) && m.getParameterCount() > 1));
        assertEquals(false, hasOrgIdWrite);

        ReputationObservation saved = service.record("example.com").get(0);
        assertEquals("org-1", saved.organizationId(), "caller cannot select another organization");
    }

    @Test
    void persistedDomainIdentityMatchesRegisteredDomain() {
        Domain domain = domains.findByName("example.com").orElseThrow();
        ReputationObservation saved = service.record("  Example.COM  ").get(0);
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void sourcePayloadAndObservedAtMatchProviderObservation() {
        Instant providerTime = Instant.parse("2026-08-08T11:00:00Z");
        postmaster.payload = "postmaster-literal";
        postmaster.observedAt = providerTime;
        postmaster.available();

        ReputationObservation saved = service.record("example.com").get(0);

        assertEquals(ReputationSource.POSTMASTER, saved.source());
        assertEquals("postmaster-literal", saved.payload());
        assertEquals(providerTime, saved.observedAt());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        postmaster.payload = "first";
        postmaster.observedAt = Instant.parse("2026-08-08T10:00:00Z");
        postmaster.available();
        ReputationObservation first = service.record("example.com").get(0);

        postmaster.payload = "second";
        postmaster.observedAt = Instant.parse("2026-08-08T11:00:00Z");
        postmaster.available();
        ReputationObservation second = service.record("example.com").get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, observations.findByDomainId(first.domainId()).size());
        ReputationObservation reloadedFirst = observations.findById(first.id()).orElseThrow();
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("first", reloadedFirst.payload());
        assertEquals("second", second.payload());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        domains.save(Domain.register("org-2", "other.com", NOW));
        ReputationObservation forOrg1 = service.record("example.com").get(0);
        ReputationObservation forOrg2 = service.record("other.com").get(0);

        List<ReputationObservation> org1 = service.findByOrganizationId("org-1");
        List<ReputationObservation> org2 = service.findByOrganizationId("org-2");
        assertEquals(1, org1.size());
        assertEquals(1, org2.size());
        assertEquals(forOrg1.id(), org1.get(0).id());
        assertEquals(forOrg2.id(), org2.get(0).id());
        assertTrue(org1.stream().allMatch(e -> e.organizationId().equals("org-1")));
        assertTrue(org2.stream().allMatch(e -> e.organizationId().equals("org-2")));
    }

    @Test
    void providerFailurePersistsNothingForThatProvider() {
        postmaster.fail();
        snds.unavailable();

        List<ReputationObservation> saved = service.record("example.com");

        assertTrue(saved.isEmpty());
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
        assertTrue(observations.findByOrganizationId("org-1").isEmpty());
    }

    @Test
    void oneProviderSuccessAndOneFailurePersistsOnlyTheRealObservation() {
        postmaster.available();
        snds.fail();

        List<ReputationObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(ReputationSource.POSTMASTER, saved.get(0).source());
        assertEquals("postmaster-raw", saved.get(0).payload());
    }

    @Test
    void bothProvidersAvailablePersistTwoSnapshots() {
        postmaster.available();
        snds.available();

        List<ReputationObservation> saved = service.record("example.com");

        assertEquals(2, saved.size());
        assertEquals(Set.of(ReputationSource.POSTMASTER, ReputationSource.SNDS),
            saved.stream().map(ReputationObservation::source).collect(Collectors.toSet()));
    }

    @Test
    void bothProvidersUnavailablePersistNothing() {
        postmaster.unavailable();
        snds.unavailable();

        assertTrue(service.record("example.com").isEmpty());
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void productionAdaptersReportUnavailableAndDoNotFabricateFacts() {
        UnavailableGooglePostmasterAdapter google = new UnavailableGooglePostmasterAdapter();
        UnavailableMicrosoftSndsAdapter microsoft = new UnavailableMicrosoftSndsAdapter();
        ReputationObservationService productionShaped = new ReputationObservationService(
            observations, domains, google, microsoft);

        assertTrue(google.query("example.com").isEmpty());
        assertTrue(microsoft.query("example.com").isEmpty());
        assertTrue(productionShaped.record("example.com").isEmpty());
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void repositoryPortHasNoUpdateOrDelete() {
        Set<String> names = Arrays.stream(ReputationObservationRepository.class.getDeclaredMethods())
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
        Class<?>[] params = ReputationObservationService.class.getConstructors()[0].getParameterTypes();
        assertEquals(ReputationObservationRepository.class, params[0]);
        assertEquals(DomainRepository.class, params[1]);
        assertEquals(GooglePostmasterPort.class, params[2]);
        assertEquals(MicrosoftSndsPort.class, params[3]);
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Adapter")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Fake")));
    }
}
