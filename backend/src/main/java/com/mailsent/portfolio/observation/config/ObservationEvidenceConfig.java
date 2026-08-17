package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.DnsObservationService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;

/**
 * Configuration and dependency wiring for observation evidence (INF-002).
 *
 * <p>There is exactly one {@link ObservationEvidenceRepository} bean per profile: the
 * PostgreSQL adapter ({@code JpaObservationEvidenceRepositoryAdapter}) is component-scanned
 * under {@code !test}; the hermetic {@link InMemoryObservationEvidenceRepository} is
 * declared here under the {@code test} profile. The {@link ObservationEvidenceService}
 * completes the wiring, reusing the existing {@link DnsObservationService} (INF-001) and
 * the shared {@link DomainRepository}.</p>
 *
 * <p>INF-002 is domain/application/persistence only — no HTTP, scheduler, or
 * authorization surface is registered.</p>
 */
@Configuration
public class ObservationEvidenceConfig {

    @Bean
    @Profile("test")
    ObservationEvidenceRepository inMemoryObservationEvidenceRepository() {
        return new InMemoryObservationEvidenceRepository();
    }

    @Bean
    ObservationEvidenceService observationEvidenceService(ObservationEvidenceRepository evidence,
                                                          DomainRepository domains,
                                                          DnsObservationService dnsObservationService) {
        return new ObservationEvidenceService(evidence, domains, dnsObservationService);
    }
}
