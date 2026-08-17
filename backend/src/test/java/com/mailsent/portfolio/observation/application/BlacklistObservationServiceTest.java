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
import com.mailsent.portfolio.observation.domain.BlacklistObservation;
import com.mailsent.portfolio.observation.domain.BlacklistSource;
import com.mailsent.portfolio.observation.infrastructure.InMemoryBlacklistObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.UnavailableBarracudaAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableSpamhausAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableSurblAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableUriblAdapter;

/**
 * Unit tests for {@link BlacklistObservationService} (INF-008) — registration
 * gate, tenant derivation from Domain, factual persistence, append-only
 * snapshots, independent provider queries, and provider-failure atomicity.
 */
class BlacklistObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryBlacklistObservationRepository observations;
    private FakeSpamhausPort spamhaus;
    private FakeBarracudaPort barracuda;
    private FakeSurblPort surbl;
    private FakeUriblPort uribl;
    private BlacklistObservationService service;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        observations = new InMemoryBlacklistObservationRepository();
        spamhaus = new FakeSpamhausPort();
        barracuda = new FakeBarracudaPort();
        surbl = new FakeSurblPort();
        uribl = new FakeUriblPort();
        service = new BlacklistObservationService(observations, domains, spamhaus, barracuda, surbl, uribl);
        domains.save(Domain.register("org-1", "example.com", NOW));
        spamhaus.available();
        barracuda.unavailable();
        surbl.unavailable();
        uribl.unavailable();
    }

    @Test
    void recordPersistsOneSnapshotForRegisteredDomainWhenOneProviderReturnsAFact() {
        List<BlacklistObservation> saved = service.record("example.com");

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
        BlacklistObservation saved = service.record("example.com").get(0);
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertEquals(domain.organizationId(), saved.organizationId());
        assertEquals("org-1", saved.organizationId());
        assertEquals(domain.id(), saved.domainId());
    }

    @Test
    void recordAcceptsOnlyDomainNameNotOrganizationId() {
        boolean hasOrgIdWrite = Arrays.stream(BlacklistObservationService.class.getDeclaredMethods())
            .filter(m -> "record".equals(m.getName()))
            .map(Method::getParameterTypes)
            .anyMatch(params -> Arrays.asList(params).contains(String.class) && params.length != 1);
        assertTrue(Arrays.stream(BlacklistObservationService.class.getMethods())
            .noneMatch(m -> "record".equals(m.getName()) && m.getParameterCount() > 1));
        assertEquals(false, hasOrgIdWrite);

        BlacklistObservation saved = service.record("example.com").get(0);
        assertEquals("org-1", saved.organizationId(), "caller cannot select another organization");
    }

    @Test
    void persistedDomainIdentityMatchesRegisteredDomain() {
        Domain domain = domains.findByName("example.com").orElseThrow();
        BlacklistObservation saved = service.record("  Example.COM  ").get(0);
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
    }

    @Test
    void sourcePayloadAndObservedAtMatchProviderObservation() {
        Instant providerTime = Instant.parse("2026-08-08T11:00:00Z");
        spamhaus.payload = "spamhaus-literal";
        spamhaus.observedAt = providerTime;
        spamhaus.available();

        BlacklistObservation saved = service.record("example.com").get(0);

        assertEquals(BlacklistSource.SPAMHAUS, saved.source());
        assertEquals("spamhaus-literal", saved.payload());
        assertEquals(providerTime, saved.observedAt());
    }

    @Test
    void secondObservationCreatesASecondRowLeavingTheFirstUnchanged() {
        spamhaus.payload = "first";
        spamhaus.observedAt = Instant.parse("2026-08-08T10:00:00Z");
        spamhaus.available();
        BlacklistObservation first = service.record("example.com").get(0);

        spamhaus.payload = "second";
        spamhaus.observedAt = Instant.parse("2026-08-08T11:00:00Z");
        spamhaus.available();
        BlacklistObservation second = service.record("example.com").get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(2, observations.findByDomainId(first.domainId()).size());
        BlacklistObservation reloadedFirst = observations.findById(first.id()).orElseThrow();
        assertEquals(first.observedAt(), reloadedFirst.observedAt());
        assertEquals("first", reloadedFirst.payload());
        assertEquals("second", second.payload());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        domains.save(Domain.register("org-2", "other.com", NOW));
        BlacklistObservation forOrg1 = service.record("example.com").get(0);
        BlacklistObservation forOrg2 = service.record("other.com").get(0);

        List<BlacklistObservation> org1 = service.findByOrganizationId("org-1");
        List<BlacklistObservation> org2 = service.findByOrganizationId("org-2");
        assertEquals(1, org1.size());
        assertEquals(1, org2.size());
        assertEquals(forOrg1.id(), org1.get(0).id());
        assertEquals(forOrg2.id(), org2.get(0).id());
        assertTrue(org1.stream().allMatch(e -> e.organizationId().equals("org-1")));
        assertTrue(org2.stream().allMatch(e -> e.organizationId().equals("org-2")));
    }

    @Test
    void allFourProvidersAreQueriedIndependently() {
        spamhaus.fail();
        barracuda.available();
        surbl.unavailable();
        uribl.fail();

        List<BlacklistObservation> saved = service.record("example.com");

        assertEquals(1, spamhaus.queryCount);
        assertEquals(1, barracuda.queryCount);
        assertEquals(1, surbl.queryCount);
        assertEquals(1, uribl.queryCount);
        assertEquals(1, saved.size());
        assertEquals(BlacklistSource.BARRACUDA, saved.get(0).source());
    }

    @Test
    void providerFailurePersistsNothingForThatProvider() {
        spamhaus.fail();
        barracuda.unavailable();
        surbl.unavailable();
        uribl.unavailable();

        List<BlacklistObservation> saved = service.record("example.com");

        assertTrue(saved.isEmpty());
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
        assertTrue(observations.findByOrganizationId("org-1").isEmpty());
    }

    @Test
    void oneProviderSuccessAndOneFailurePersistsOnlyTheRealObservation() {
        spamhaus.available();
        barracuda.fail();
        surbl.unavailable();
        uribl.unavailable();

        List<BlacklistObservation> saved = service.record("example.com");

        assertEquals(1, saved.size());
        assertEquals(BlacklistSource.SPAMHAUS, saved.get(0).source());
        assertEquals("spamhaus-raw", saved.get(0).payload());
    }

    @Test
    void allProvidersAvailablePersistFourSnapshots() {
        spamhaus.available();
        barracuda.available();
        surbl.available();
        uribl.available();

        List<BlacklistObservation> saved = service.record("example.com");

        assertEquals(4, saved.size());
        assertEquals(Set.of(BlacklistSource.SPAMHAUS, BlacklistSource.BARRACUDA, BlacklistSource.SURBL,
                BlacklistSource.URIBL),
            saved.stream().map(BlacklistObservation::source).collect(Collectors.toSet()));
    }

    @Test
    void allProvidersUnavailablePersistNothing() {
        spamhaus.unavailable();
        barracuda.unavailable();
        surbl.unavailable();
        uribl.unavailable();

        assertTrue(service.record("example.com").isEmpty());
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void productionAdaptersReportUnavailableAndDoNotFabricateFacts() {
        UnavailableSpamhausAdapter spamhausAdapter = new UnavailableSpamhausAdapter();
        UnavailableBarracudaAdapter barracudaAdapter = new UnavailableBarracudaAdapter();
        UnavailableSurblAdapter surblAdapter = new UnavailableSurblAdapter();
        UnavailableUriblAdapter uriblAdapter = new UnavailableUriblAdapter();
        BlacklistObservationService productionShaped = new BlacklistObservationService(
            observations, domains, spamhausAdapter, barracudaAdapter, surblAdapter, uriblAdapter);

        assertTrue(spamhausAdapter.query("example.com").isEmpty());
        assertTrue(barracudaAdapter.query("example.com").isEmpty());
        assertTrue(surblAdapter.query("example.com").isEmpty());
        assertTrue(uriblAdapter.query("example.com").isEmpty());
        assertTrue(productionShaped.record("example.com").isEmpty());
        Domain domain = domains.findByName("example.com").orElseThrow();
        assertTrue(observations.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void repositoryPortHasNoUpdateOrDelete() {
        Set<String> names = Arrays.stream(BlacklistObservationRepository.class.getDeclaredMethods())
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
        Class<?>[] params = BlacklistObservationService.class.getConstructors()[0].getParameterTypes();
        assertEquals(BlacklistObservationRepository.class, params[0]);
        assertEquals(DomainRepository.class, params[1]);
        assertEquals(SpamhausPort.class, params[2]);
        assertEquals(BarracudaPort.class, params[3]);
        assertEquals(SurblPort.class, params[4]);
        assertEquals(UriblPort.class, params[5]);
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Adapter")));
        assertTrue(Arrays.stream(params).noneMatch(p -> p.getSimpleName().contains("Fake")));
    }
}
