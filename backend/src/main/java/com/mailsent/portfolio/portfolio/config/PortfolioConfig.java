package com.mailsent.portfolio.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.application.PortfolioService;

/**
 * Configuration and dependency wiring for the Portfolio module (PORT-001).
 *
 * <p>Portfolio is a projection over Client Relationships, so it needs no repository of its
 * own; {@link PortfolioService} reads through the shared {@link ClientRelationshipRepository}
 * (the in-memory adapter under the {@code test} profile, the JPA adapter otherwise) and the
 * shared {@link OrganizationRepository} (to verify the owning Agency exists). PORT-001 is
 * domain/application only — no API contract, no controller, no Portfolio persistence.</p>
 */
@Configuration
public class PortfolioConfig {

    @Bean
    PortfolioService portfolioService(ClientRelationshipRepository clientRelationships,
                                     OrganizationRepository organizations) {
        return new PortfolioService(clientRelationships, organizations);
    }

    @Bean
    PortfolioOverviewService portfolioOverviewService(ClientRelationshipRepository clientRelationships,
                                                      DomainRepository domains, OrganizationRepository organizations,
                                                      HealthSignalService healthSignals) {
        return new PortfolioOverviewService(clientRelationships, domains, organizations, healthSignals);
    }
}