package com.mailsent.portfolio.observation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.DnsHealthObservationRepository;
import com.mailsent.portfolio.observation.application.DnsHealthObservationService;
import com.mailsent.portfolio.observation.application.DnsHealthObserver;
import com.mailsent.portfolio.observation.application.NamedDnsResolverQuery;
import com.mailsent.portfolio.observation.infrastructure.DnsJavaDnsHealthObserver;
import com.mailsent.portfolio.observation.infrastructure.DnsJavaNamedResolverQuery;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDnsHealthObservationRepository;

/**
 * Configuration and dependency wiring for DNS health observation (INF-007).
 *
 * <p>There is exactly one {@link DnsHealthObservationRepository} bean per
 * profile: the PostgreSQL adapter ({@code JpaDnsHealthObservationRepositoryAdapter})
 * is component-scanned under {@code !test}; the hermetic
 * {@link InMemoryDnsHealthObservationRepository} is declared here under the
 * {@code test} profile. Production observers use dnsjava; test doubles belong
 * in test sources and override these beans when needed.</p>
 *
 * <p>INF-007 is domain/application/persistence only — no HTTP, scheduler, or
 * authorization surface is registered. INF-004 is not modified.</p>
 */
@Configuration
public class DnsHealthObservationConfig {

    @Bean
    @Profile("test")
    DnsHealthObservationRepository inMemoryDnsHealthObservationRepository() {
        return new InMemoryDnsHealthObservationRepository();
    }

    @Bean
    @ConditionalOnMissingBean(NamedDnsResolverQuery.class)
    NamedDnsResolverQuery namedDnsResolverQuery() {
        return new DnsJavaNamedResolverQuery();
    }

    @Bean
    @ConditionalOnMissingBean(DnsHealthObserver.class)
    DnsHealthObserver dnsHealthObserver(NamedDnsResolverQuery namedDnsResolverQuery) {
        return new DnsJavaDnsHealthObserver(namedDnsResolverQuery);
    }

    @Bean
    DnsHealthObservationService dnsHealthObservationService(DnsHealthObservationRepository observations,
                                                            DomainRepository domains,
                                                            DnsHealthObserver observer) {
        return new DnsHealthObservationService(observations, domains, observer);
    }
}
