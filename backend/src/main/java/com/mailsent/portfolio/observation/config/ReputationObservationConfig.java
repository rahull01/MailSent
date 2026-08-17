package com.mailsent.portfolio.observation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.GooglePostmasterPort;
import com.mailsent.portfolio.observation.application.MicrosoftSndsPort;
import com.mailsent.portfolio.observation.application.ReputationObservationRepository;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReputationObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.UnavailableGooglePostmasterAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableMicrosoftSndsAdapter;

/**
 * Configuration and dependency wiring for reputation observation (INF-003).
 *
 * <p>There is exactly one {@link ReputationObservationRepository} bean per
 * profile: the PostgreSQL adapter ({@code JpaReputationObservationRepositoryAdapter})
 * is component-scanned under {@code !test}; the hermetic
 * {@link InMemoryReputationObservationRepository} is declared here under the
 * {@code test} profile. Provider ports are application interfaces; production
 * adapters report unavailability until a verified provider API exists. Test
 * doubles belong in test sources and override these beans when needed.</p>
 *
 * <p>INF-003 is domain/application/persistence only — no HTTP, scheduler, or
 * authorization surface is registered.</p>
 */
@Configuration
public class ReputationObservationConfig {

    @Bean
    @Profile("test")
    ReputationObservationRepository inMemoryReputationObservationRepository() {
        return new InMemoryReputationObservationRepository();
    }

    @Bean
    @ConditionalOnMissingBean(GooglePostmasterPort.class)
    GooglePostmasterPort googlePostmasterPort() {
        return new UnavailableGooglePostmasterAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(MicrosoftSndsPort.class)
    MicrosoftSndsPort microsoftSndsPort() {
        return new UnavailableMicrosoftSndsAdapter();
    }

    @Bean
    ReputationObservationService reputationObservationService(ReputationObservationRepository observations,
                                                              DomainRepository domains,
                                                              GooglePostmasterPort postmaster,
                                                              MicrosoftSndsPort snds) {
        return new ReputationObservationService(observations, domains, postmaster, snds);
    }
}
