package com.mailsent.portfolio.observation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.BarracudaPort;
import com.mailsent.portfolio.observation.application.BlacklistObservationRepository;
import com.mailsent.portfolio.observation.application.BlacklistObservationService;
import com.mailsent.portfolio.observation.application.SpamhausPort;
import com.mailsent.portfolio.observation.application.SurblPort;
import com.mailsent.portfolio.observation.application.UriblPort;
import com.mailsent.portfolio.observation.infrastructure.InMemoryBlacklistObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.UnavailableBarracudaAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableSpamhausAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableSurblAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableUriblAdapter;

/**
 * Configuration and dependency wiring for blacklist observation (INF-008).
 *
 * <p>There is exactly one {@link BlacklistObservationRepository} bean per
 * profile: the PostgreSQL adapter ({@code JpaBlacklistObservationRepositoryAdapter})
 * is component-scanned under {@code !test}; the hermetic
 * {@link InMemoryBlacklistObservationRepository} is declared here under the
 * {@code test} profile. Provider ports are application interfaces; production
 * adapters report unavailability until a verified provider API exists. Test
 * doubles belong in test sources and override these beans when needed.</p>
 *
 * <p>INF-008 is domain/application/persistence only — no HTTP, scheduler, or
 * authorization surface is registered.</p>
 */
@Configuration
public class BlacklistObservationConfig {

    @Bean
    @Profile("test")
    BlacklistObservationRepository inMemoryBlacklistObservationRepository() {
        return new InMemoryBlacklistObservationRepository();
    }

    @Bean
    @ConditionalOnMissingBean(SpamhausPort.class)
    SpamhausPort spamhausPort() {
        return new UnavailableSpamhausAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(BarracudaPort.class)
    BarracudaPort barracudaPort() {
        return new UnavailableBarracudaAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(SurblPort.class)
    SurblPort surblPort() {
        return new UnavailableSurblAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(UriblPort.class)
    UriblPort uriblPort() {
        return new UnavailableUriblAdapter();
    }

    @Bean
    BlacklistObservationService blacklistObservationService(BlacklistObservationRepository observations,
                                                            DomainRepository domains,
                                                            SpamhausPort spamhaus,
                                                            BarracudaPort barracuda,
                                                            SurblPort surbl,
                                                            UriblPort uribl) {
        return new BlacklistObservationService(observations, domains, spamhaus, barracuda, surbl, uribl);
    }
}
