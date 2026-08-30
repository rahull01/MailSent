package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.application.GooglePostmasterPort;
import com.mailsent.portfolio.observation.application.MicrosoftSndsPort;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReObservationScheduler;
import com.mailsent.portfolio.observation.application.ReObservationService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.application.ReputationProviderObservation;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration test for INF-004 re-observation through the
 * application {@code DomainRepository} port (no direct JPA {@code findAll}).
 * Gated on {@code mailsent.it.postgres}. The {@code it} profile does not
 * register {@code @EnableScheduling} or the scheduler bean, so the daily job
 * does not fire; this test invokes {@link ReObservationService} directly.
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ReObservationPersistenceIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-08T10:00:00Z");

    @Autowired private ApplicationContext context;
    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ReObservationService reObservationService;
    @Autowired private ObservationEvidenceService evidenceService;
    @Autowired private ReputationObservationService reputationService;
    @Autowired private com.mailsent.identity.consent.application.ConsentService consentService;
    @Autowired private com.mailsent.identity.authentication.application.UserRepository userRepository;

    @TestConfiguration
    static class FakePortConfig {
        @Bean
        SequenceResolver sequenceResolver() {
            return new SequenceResolver();
        }

        @Bean
        @Primary
        DnsResolver fakeDnsResolver(SequenceResolver sequence) {
            return sequence;
        }

        @Bean
        @Primary
        GooglePostmasterPort fakePostmaster() {
            return new SequencePostmaster();
        }

        @Bean
        @Primary
        MicrosoftSndsPort fakeSnds() {
            return new SequenceSnds();
        }
    }

    static final class SequenceResolver implements DnsResolver {
        private final AtomicInteger seq = new AtomicInteger();

        @Override
        public DnsObservation observe(String domainName) {
            Instant observedAt = BASE.plusSeconds(seq.getAndIncrement());
            return DnsObservation.of(domainName, List.of(new MxRecord(10, "mail.example.com")),
                List.of("v=spf1 -all"), "v=spf1 -all", observedAt);
        }
    }

    static final class SequencePostmaster implements GooglePostmasterPort {
        @Override
        public Optional<ReputationProviderObservation> query(String domainName) {
            return Optional.of(new ReputationProviderObservation("it-reobserve-postmaster", BASE));
        }
    }

    static final class SequenceSnds implements MicrosoftSndsPort {
        @Override
        public Optional<ReputationProviderObservation> query(String domainName) {
            return Optional.empty();
        }
    }

    @Test
    void itProfileDoesNotRegisterSchedulerOrScheduledProcessing() {
        assertEquals(0, context.getBeanNamesForType(ReObservationScheduler.class).length);
        assertEquals(0, context.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class).length);
    }

    @Test
    void reobserveAllPersistsDnsAndReputationSnapshotsForRegisteredDomain() {
        String orgId = organizationService.create("Reobserve " + UUID.randomUUID()).id();
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
        Domain domain = domainService.register(orgId, "it-reobserve-" + UUID.randomUUID() + ".com");
        consentService.grantConsent(orgId, domain.id(), user.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        reObservationService.reobserveAll();

        List<ObservationEvidence> evidence = evidenceService.findByDomainId(domain.id());
        assertTrue(!evidence.isEmpty());
        assertTrue(evidence.stream().allMatch(e -> e.organizationId().equals(orgId)));

        List<ReputationObservation> reputation = reputationService.findByDomainId(domain.id());
        assertTrue(reputation.stream().anyMatch(r -> r.source() == ReputationSource.POSTMASTER
            && r.payload().equals("it-reobserve-postmaster")));
        assertTrue(reputation.stream().noneMatch(r -> r.source() == ReputationSource.SNDS));
    }
}
