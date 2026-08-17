package com.mailsent.portfolio.observation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.ForwardAddressLookup;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.application.SmtpGreetingObservationRepository;
import com.mailsent.portfolio.observation.application.SmtpGreetingObservationService;
import com.mailsent.portfolio.observation.application.SmtpProbe;
import com.mailsent.portfolio.observation.infrastructure.InMemorySmtpGreetingObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.SocketSmtpProbe;

/**
 * Configuration and dependency wiring for SMTP greeting observation (INF-006).
 *
 * <p>There is exactly one {@link SmtpGreetingObservationRepository} bean per
 * profile: the PostgreSQL adapter ({@code JpaSmtpGreetingObservationRepositoryAdapter})
 * is component-scanned under {@code !test}; the hermetic
 * {@link InMemorySmtpGreetingObservationRepository} is declared here under the
 * {@code test} profile. Production probing uses a bounded JDK {@link SocketSmtpProbe}
 * to MX IPs on port 25. {@link ForwardAddressLookup} is reused from INF-005;
 * this configuration does not declare that bean. Test doubles belong in test
 * sources and override these beans when needed.</p>
 *
 * <p>INF-006 is domain/application/persistence only — no HTTP, scheduler, or
 * authorization surface is registered. INF-004 is not modified. Observation
 * Evidence, Reverse DNS Observation, and ReObservationConfig are not
 * modified.</p>
 */
@Configuration
public class SmtpGreetingObservationConfig {

    @Bean
    @Profile("test")
    SmtpGreetingObservationRepository inMemorySmtpGreetingObservationRepository() {
        return new InMemorySmtpGreetingObservationRepository();
    }

    @Bean
    @ConditionalOnMissingBean(SmtpProbe.class)
    SmtpProbe smtpProbe() {
        return new SocketSmtpProbe();
    }

    @Bean
    SmtpGreetingObservationService smtpGreetingObservationService(SmtpGreetingObservationRepository observations,
                                                                  DomainRepository domains,
                                                                  ObservationEvidenceRepository evidence,
                                                                  ForwardAddressLookup forward,
                                                                  SmtpProbe smtp,
                                                                  com.mailsent.identity.consent.application.ConsentService consentService) {
        return new SmtpGreetingObservationService(observations, domains, evidence, forward, smtp, consentService);
    }
}
