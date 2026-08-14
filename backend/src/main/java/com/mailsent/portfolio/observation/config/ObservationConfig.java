package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.DnsObservationService;
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.infrastructure.DnsJavaDnsResolver;

/**
 * Configuration and dependency wiring for the observation module (INF-001).
 *
 * <p>The {@link DnsResolver} is the dnsjava-backed adapter; the {@link DnsObservationService}
 * completes the wiring, gating observation on the shared {@link DomainRepository} (only
 * registered domains may be observed). INF-001 is a read-only capability — no persistence,
 * no HTTP, no authorization (AUTH-006 deferred).</p>
 */
@Configuration
public class ObservationConfig {

    @Bean
    DnsResolver dnsResolver() {
        return new DnsJavaDnsResolver();
    }

    @Bean
    DnsObservationService dnsObservationService(DnsResolver resolver, DomainRepository domains) {
        return new DnsObservationService(resolver, domains);
    }
}