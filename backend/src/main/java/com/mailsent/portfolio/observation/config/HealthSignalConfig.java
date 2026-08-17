package com.mailsent.portfolio.observation.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.application.HealthSignalRepository;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.application.ReputationObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryHealthSignalRepository;

/**
 * Configuration and dependency wiring for Health Signal (HEALTH-001).
 *
 * <p>There is exactly one {@link HealthSignalRepository} bean per profile: the
 * PostgreSQL adapter is component-scanned under {@code !test}; the hermetic
 * {@link InMemoryHealthSignalRepository} is declared here under the
 * {@code test} profile.</p>
 *
 * <p>HEALTH-001 is domain/application/persistence only — no HTTP, scheduler,
 * or authorization surface is registered. INF-004 and INT-006 are not
 * extended.</p>
 */
@Configuration
public class HealthSignalConfig {

    @Bean
    @Profile("test")
    HealthSignalRepository inMemoryHealthSignalRepository() {
        return new InMemoryHealthSignalRepository();
    }

    @Bean
    HealthSignalService healthSignalService(HealthSignalRepository signals, DomainRepository domains,
                                            ObservationEvidenceRepository observationEvidence,
                                            DmarcAggregateEvidenceRepository aggregateEvidence,
                                            ReputationObservationRepository reputationObservations,
                                            Clock clock) {
        return new HealthSignalService(
            signals, domains, observationEvidence, aggregateEvidence, reputationObservations, clock);
    }
}
