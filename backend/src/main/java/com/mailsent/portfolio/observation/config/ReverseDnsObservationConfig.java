package com.mailsent.portfolio.observation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.ForwardAddressLookup;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.application.ReverseDnsLookup;
import com.mailsent.portfolio.observation.application.ReverseDnsObservationRepository;
import com.mailsent.portfolio.observation.application.ReverseDnsObservationService;
import com.mailsent.portfolio.observation.infrastructure.DnsJavaForwardAddressLookup;
import com.mailsent.portfolio.observation.infrastructure.DnsJavaReverseDnsLookup;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReverseDnsObservationRepository;

/**
 * Configuration and dependency wiring for reverse DNS observation (INF-005).
 *
 * <p>There is exactly one {@link ReverseDnsObservationRepository} bean per
 * profile: the PostgreSQL adapter ({@code JpaReverseDnsObservationRepositoryAdapter})
 * is component-scanned under {@code !test}; the hermetic
 * {@link InMemoryReverseDnsObservationRepository} is declared here under the
 * {@code test} profile. Production lookups use dnsjava Type.A, Type.AAAA, and
 * Type.PTR; test doubles belong in test sources and override these beans when
 * needed.</p>
 *
 * <p>INF-005 is domain/application/persistence only — no HTTP, scheduler, or
 * authorization surface is registered. INF-004 is not modified. Observation
 * Evidence, ObservationConfig, and ReObservationConfig are not modified.</p>
 */
@Configuration
public class ReverseDnsObservationConfig {

    @Bean
    @Profile("test")
    ReverseDnsObservationRepository inMemoryReverseDnsObservationRepository() {
        return new InMemoryReverseDnsObservationRepository();
    }

    @Bean
    @ConditionalOnMissingBean(ForwardAddressLookup.class)
    ForwardAddressLookup forwardAddressLookup() {
        return new DnsJavaForwardAddressLookup();
    }

    @Bean
    @ConditionalOnMissingBean(ReverseDnsLookup.class)
    ReverseDnsLookup reverseDnsLookup() {
        return new DnsJavaReverseDnsLookup();
    }

    @Bean
    ReverseDnsObservationService reverseDnsObservationService(ReverseDnsObservationRepository observations,
                                                              DomainRepository domains,
                                                              ObservationEvidenceRepository evidence,
                                                              ForwardAddressLookup forward,
                                                              ReverseDnsLookup reverse,
                                                              com.mailsent.identity.consent.application.ConsentService consentService) {
        return new ReverseDnsObservationService(observations, domains, evidence, forward, reverse, consentService);
    }
}
