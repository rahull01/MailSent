package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;

/**
 * Configuration and dependency wiring for DMARC Aggregate Evidence (INT-005).
 *
 * <p>There is exactly one {@link DmarcAggregateEvidenceRepository} bean per
 * profile: the PostgreSQL adapter is component-scanned under {@code !test};
 * the hermetic {@link InMemoryDmarcAggregateEvidenceRepository} is declared
 * here under the {@code test} profile.</p>
 *
 * <p>INT-005 is domain/application/persistence only — no HTTP, scheduler, or
 * wiring into INT-003 receive. INF-004 is not extended. Observation Evidence,
 * Alignment, Health Signal, and DIAG-001 are out of scope.</p>
 */
@Configuration
public class DmarcAggregateEvidenceConfig {

    @Bean
    @Profile("test")
    DmarcAggregateEvidenceRepository inMemoryDmarcAggregateEvidenceRepository() {
        return new InMemoryDmarcAggregateEvidenceRepository();
    }

    @Bean
    DmarcAggregateEvidenceService dmarcAggregateEvidenceService(DmarcAggregateEvidenceRepository evidence) {
        return new DmarcAggregateEvidenceService(evidence);
    }
}
