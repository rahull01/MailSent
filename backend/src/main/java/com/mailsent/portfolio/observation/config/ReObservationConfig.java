package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReObservationScheduler;
import com.mailsent.portfolio.observation.application.ReObservationService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;

/**
 * Configuration and dependency wiring for scheduled re-observation (INF-004).
 *
 * <p>The {@link ReObservationService} iterates registered domains through the
 * application {@link DomainRepository} port and delegates to the existing
 * INF-002 and INF-003 record use cases. The {@link ReObservationScheduler}
 * is the Spring-native trigger — no HTTP, no authorization, and not MON-001.
 * The scheduler bean is omitted under {@code test} and {@code it}; the service
 * remains available so tests can invoke re-observation directly.</p>
 */
@Configuration
public class ReObservationConfig {

    @Bean
    ReObservationService reObservationService(DomainRepository domains,
                                              ObservationEvidenceService evidence,
                                              ReputationObservationService reputation,
                                              com.mailsent.identity.consent.application.ConsentService consentService) {
        return new ReObservationService(domains, evidence, reputation, consentService);
    }

    @Bean
    @Profile("!test & !it")
    ReObservationScheduler reObservationScheduler(ReObservationService reObservation) {
        return new ReObservationScheduler(reObservation);
    }
}
