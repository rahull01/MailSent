package com.mailsent.portfolio.organization.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Configuration and dependency wiring for the Organization module (ORG-001).
 *
 * <p>There is exactly one {@link OrganizationRepository} bean per profile: the PostgreSQL
 * adapter ({@code JpaOrganizationRepositoryAdapter}) is component-scanned under
 * {@code !test}; the hermetic {@link InMemoryOrganizationRepository} is declared here
 * under the {@code test} profile. The {@link OrganizationService} and the shared
 * {@link Clock} (from the authentication module config) complete the wiring.</p>
 *
 * <p>ORG-001 is domain/application/persistence only — the frozen documentation defines no
 * organization API contract, so no controller is registered.</p>
 */
@Configuration
public class OrganizationConfig {

    @Bean
    @Profile("test")
    OrganizationRepository inMemoryOrganizationRepository() {
        return new InMemoryOrganizationRepository();
    }

    @Bean
    OrganizationService organizationService(OrganizationRepository repository, Clock clock) {
        return new OrganizationService(repository, clock);
    }
}