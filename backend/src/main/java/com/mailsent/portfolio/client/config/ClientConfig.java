package com.mailsent.portfolio.client.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.infrastructure.InMemoryClientRelationshipRepository;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * Configuration and dependency wiring for the Client Relationship module (CLIENT-001).
 *
 * <p>There is exactly one {@link ClientRelationshipRepository} per profile: the PostgreSQL
 * adapter ({@code JpaClientRelationshipRepositoryAdapter}) is component-scanned under
 * {@code !test}; the hermetic {@link InMemoryClientRelationshipRepository} is declared here
 * under the {@code test} profile. The {@link ClientRelationshipService} completes the
 * wiring, taking the shared {@link OrganizationRepository} (to verify both referenced
 * organizations exist) and the shared {@link Clock}.</p>
 *
 * <p>CLIENT-001 is domain/application/persistence only — there is no API contract, so no
 * controller is registered.</p>
 */
@Configuration
public class ClientConfig {

    @Bean
    @Profile("test")
    ClientRelationshipRepository inMemoryClientRelationshipRepository() {
        return new InMemoryClientRelationshipRepository();
    }

    @Bean
    ClientRelationshipService clientRelationshipService(ClientRelationshipRepository relationships,
                                                        OrganizationRepository organizations, Clock clock) {
        return new ClientRelationshipService(relationships, organizations, clock);
    }
}