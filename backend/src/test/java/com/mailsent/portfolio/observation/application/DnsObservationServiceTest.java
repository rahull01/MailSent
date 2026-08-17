package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DnsObservation;

/**
 * Unit tests for {@link DnsObservationService} (INF-001) — the registration gate and the
 * delegation to the resolver port, using a deterministic {@link DnsResolver} double.
 */
class DnsObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    private InMemoryDomainRepository domains;
    private RecordingResolver resolver;
    private DnsObservationService service;
    private com.mailsent.identity.consent.application.ConsentService consentService;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        resolver = new RecordingResolver();
        consentService = org.mockito.Mockito.mock(com.mailsent.identity.consent.application.ConsentService.class);
        service = new DnsObservationService(resolver, domains, consentService);
        domains.save(Domain.register("org-1", "example.com", NOW));
    }

    @Test
    void observeCallsResolverWithCanonicalNameForRegisteredDomain() {
        DnsObservation observation = service.observe("  Example.COM  ");

        assertEquals("example.com", resolver.lastObserved(), "the resolver receives the canonical domain");
        assertEquals("example.com", observation.domainName());
    }

    @Test
    void observeReturnsRealObservationFromResolver() {
        DnsObservation observation = service.observe("example.com");
        assertEquals("example.com", observation.domainName());
        assertTrue(observation.mxRecords().isEmpty());
    }

    @Test
    void observeRejectsUnregisteredDomainWithoutCallingResolver() {
        assertThrows(DomainNotRegisteredException.class, () -> service.observe("not-registered.com"));
        assertEquals(null, resolver.lastObserved(), "the resolver is never called for an unregistered domain");
    }

    /** A deterministic resolver double that records the observed name. */
    private static final class RecordingResolver implements DnsResolver {
        private String lastObserved;

        @Override
        public DnsObservation observe(String domainName) {
            this.lastObserved = domainName;
            return DnsObservation.of(domainName, List.of(), List.of(), null, NOW);
        }

        String lastObserved() {
            return lastObserved;
        }
    }
}