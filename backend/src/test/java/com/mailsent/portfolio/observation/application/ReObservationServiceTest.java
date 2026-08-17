package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;

/**
 * Unit tests for {@link ReObservationService} (INF-004) — re-observes DNS then
 * reputation for every registered domain, continues on expected per-domain
 * failures, and never fabricates or produces a Health Signal.
 */
class ReObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private ObservationEvidenceService evidence;
    private ReputationObservationService reputation;
    private ReObservationService service;
    private com.mailsent.identity.consent.application.ConsentService consentService;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        evidence = mock(ObservationEvidenceService.class);
        reputation = mock(ReputationObservationService.class);
        consentService = mock(com.mailsent.identity.consent.application.ConsentService.class);
        service = new ReObservationService(domains, evidence, reputation, consentService);
    }

    @Test
    void reobserveAllIsNoOpWhenNoDomainsAreRegistered() {
        service.reobserveAll();
        verifyNoInteractions(evidence, reputation);
    }

    @Test
    void reobserveAllRecordsDnsThenReputationForEachDomain() {
        Domain first = domains.save(Domain.register("org-1", "alpha.com", NOW));
        Domain second = domains.save(Domain.register("org-2", "beta.com", NOW));

        service.reobserveAll();

        verify(evidence).record(first.name());
        verify(evidence).record(second.name());
        verify(reputation).record(first.name());
        verify(reputation).record(second.name());

        InOrder alpha = inOrder(evidence, reputation);
        alpha.verify(evidence).record("alpha.com");
        alpha.verify(reputation).record("alpha.com");
    }

    @Test
    void dnsFailureContinuesToReputationAndToTheNextDomain() {
        domains.save(Domain.register("org-1", "fail.com", NOW));
        domains.save(Domain.register("org-2", "ok.com", NOW));
        doThrow(new DnsObservationException("lookup failed")).when(evidence).record("fail.com");

        service.reobserveAll();

        verify(reputation).record("fail.com");
        verify(evidence).record("ok.com");
        verify(reputation).record("ok.com");
    }

    @Test
    void domainNotRegisteredOnEvidenceContinues() {
        domains.save(Domain.register("org-1", "gone.com", NOW));
        doThrow(new DomainNotRegisteredException("gone.com")).when(evidence).record("gone.com");

        service.reobserveAll();

        verify(reputation).record("gone.com");
    }

    @Test
    void domainNotRegisteredOnReputationContinuesToNextDomain() {
        domains.save(Domain.register("org-1", "gone.com", NOW));
        domains.save(Domain.register("org-2", "ok.com", NOW));
        doThrow(new DomainNotRegisteredException("gone.com")).when(reputation).record("gone.com");

        service.reobserveAll();

        verify(evidence).record("ok.com");
        verify(reputation).record("ok.com");
    }

    @Test
    void unexpectedRuntimeExceptionFromEvidenceIsNotSwallowed() {
        com.mailsent.portfolio.domain.application.DomainRepository mockedDomains = mock(com.mailsent.portfolio.domain.application.DomainRepository.class);
        ReObservationService serviceWithMock = new ReObservationService(mockedDomains, evidence, reputation, consentService);

        Domain boom = Domain.register("org-1", "boom.com", NOW);
        Domain later = Domain.register("org-2", "later.com", NOW);
        org.mockito.Mockito.when(mockedDomains.findAll()).thenReturn(java.util.List.of(boom, later));

        doThrow(new IllegalStateException("unexpected")).when(evidence).record("boom.com");

        assertThrows(IllegalStateException.class, () -> serviceWithMock.reobserveAll());
        verify(reputation, never()).record("boom.com");
        verify(evidence, never()).record("later.com");
    }

    @Test
    void errorFromEvidenceIsNotCaughtAsThrowable() {
        domains.save(Domain.register("org-1", "fatal.com", NOW));
        doThrow(new AssertionError("must propagate")).when(evidence).record("fatal.com");

        assertThrows(AssertionError.class, () -> service.reobserveAll());
    }

    @Test
    void doesNotDependOnHealthSignal() {
        assertTrue(java.util.Arrays.stream(ReObservationService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Health")));
        domains.save(Domain.register("org-1", "example.com", NOW));
        service.reobserveAll();
        verify(evidence, times(1)).record("example.com");
        verify(reputation, times(1)).record("example.com");
    }
}
