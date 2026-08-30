package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.BlacklistObservationService;
import com.mailsent.portfolio.observation.application.DnsHealthObservationService;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.application.InfrastructureMapService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;

/**
 * Configuration and dependency wiring for the Infrastructure Map topology
 * read-model (MAP-001 / MAP-002).
 *
 * <p>MAP-001/MAP-002 is application-only: no HTTP, persistence, scheduler, or
 * authorization surface is registered. It composes existing observation and health
 * services; it does not change INF-001–008 or HEALTH-001.</p>
 */
@Configuration
public class InfrastructureMapConfig {

    @Bean
    InfrastructureMapService infrastructureMapService(DomainRepository domains,
                                                      ObservationEvidenceService evidence,
                                                      ReputationObservationService reputation,
                                                      DnsHealthObservationService dnsHealth,
                                                      BlacklistObservationService blacklist,
                                                      HealthSignalService healthSignalService) {
        return new InfrastructureMapService(domains, evidence, reputation, dnsHealth, blacklist, healthSignalService);
    }
}
