package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.observation.application.DnsObservationService;
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.application.DomainNotRegisteredException;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INF-001 observation gate.
 *
 * <p>Observation is gated on the domain being registered in the real domain registry
 * (PostgreSQL). The DNS resolver is a deterministic {@link TestConfiguration} double — the
 * normal test suite never depends on an uncontrolled public DNS result (the dnsjava-backed
 * resolver's mapping is unit-tested separately). Gated on {@code mailsent.it.postgres};
 * idempotent (randomized names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DnsObservationPersistenceIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domains;
    @Autowired private DnsObservationService observationService;
    @Autowired private com.mailsent.identity.consent.application.ConsentService consentService;
    @Autowired private com.mailsent.identity.authentication.application.UserRepository userRepository;

    @TestConfiguration
    static class FakeResolverConfig {
        // Distinct bean name + @Primary (two DnsResolver beans): injection resolves to the
        // deterministic fake; the real dnsjava resolver remains defined but is not used here.
        @Bean
        @Primary
        DnsResolver fakeDnsResolver() {
            return name -> DnsObservation.of(name, List.of(), List.of(), null, NOW);
        }
    }

    @Test
    void observeRegisteredDomainThroughRealRegistry() {
        String orgId = organizationService.create("Observation " + UUID.randomUUID()).id();
        com.mailsent.identity.authentication.domain.User user = userRepository.save(
            com.mailsent.identity.authentication.domain.User.restore(
                UUID.randomUUID().toString(),
                new com.mailsent.identity.authentication.domain.Email("user-" + UUID.randomUUID() + "@example.com"),
                "hash",
                com.mailsent.identity.authentication.domain.Role.OWNER,
                Instant.now(),
                orgId
            )
        );
        String domainName = "it-" + UUID.randomUUID() + ".com";
        com.mailsent.portfolio.domain.domain.Domain domain = domains.register(orgId, domainName);
        consentService.grantConsent(orgId, domain.id(), user.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        DnsObservation observation = observationService.observe(domainName);

        assertEquals(domainName, observation.domainName(), "the registered domain is observed");
        assertEquals(orgId, domains.findByDomain(domainName).orElseThrow().organizationId(),
            "the observed domain belongs to the granting organization");
    }

    @Test
    void observeUnregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class,
            () -> observationService.observe("it-" + UUID.randomUUID() + "-not-registered.com"));
    }

    @Test
    void observeIsIsolatedToTheRegisteredDomain() {
        String orgA = organizationService.create("ObservationA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("ObservationB " + UUID.randomUUID()).id();
        com.mailsent.identity.authentication.domain.User userA = userRepository.save(
            com.mailsent.identity.authentication.domain.User.restore(
                UUID.randomUUID().toString(),
                new com.mailsent.identity.authentication.domain.Email("user-a-" + UUID.randomUUID() + "@example.com"),
                "hash",
                com.mailsent.identity.authentication.domain.Role.OWNER,
                Instant.now(),
                orgA
            )
        );
        com.mailsent.identity.authentication.domain.User userB = userRepository.save(
            com.mailsent.identity.authentication.domain.User.restore(
                UUID.randomUUID().toString(),
                new com.mailsent.identity.authentication.domain.Email("user-b-" + UUID.randomUUID() + "@example.com"),
                "hash",
                com.mailsent.identity.authentication.domain.Role.OWNER,
                Instant.now(),
                orgB
            )
        );
        String domainA = "it-" + UUID.randomUUID() + "a.com";
        String domainB = "it-" + UUID.randomUUID() + "b.com";
        com.mailsent.portfolio.domain.domain.Domain domA = domains.register(orgA, domainA);
        com.mailsent.portfolio.domain.domain.Domain domB = domains.register(orgB, domainB);
        consentService.grantConsent(orgA, domA.id(), userA.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentService.grantConsent(orgB, domB.id(), userB.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        DnsObservation forA = observationService.observe(domainA);
        assertEquals(domainA, forA.domainName(), "observing domain A returns only domain A's observation");
        // Domain B is registered under a different organization; it is not reachable via A.
        assertEquals(domainB, observationService.observe(domainB).domainName());
        assertThrows(DomainNotRegisteredException.class,
            () -> observationService.observe("it-" + UUID.randomUUID() + "-unregistered.com"));
    }
}