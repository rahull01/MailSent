package com.mailsent.identity.team.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.application.InvitationService;
import com.mailsent.identity.team.domain.InvitationTokenFactory;
import com.mailsent.identity.team.infrastructure.InMemoryInvitationRepository;
import com.mailsent.identity.team.infrastructure.SecureInvitationTokenFactory;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * Configuration and dependency wiring for the team module (TEAM-001, Identity context).
 *
 * <p>There is exactly one {@link InvitationRepository} per profile: the PostgreSQL adapter
 * ({@code JpaInvitationRepositoryAdapter}) is component-scanned under {@code !test}; the
 * hermetic {@link InMemoryInvitationRepository} is declared here under the {@code test}
 * profile. The {@link InvitationService} completes the wiring, taking the shared
 * {@link OrganizationRepository}, {@link UserRepository}, {@link Clock}, and a secure
 * {@link InvitationTokenFactory}. (No membership model exists in TEAM-001.)</p>
 *
 * <p>TEAM-001 exposes a single HTTP accept surface ({@code InvitationAcceptController});
 * invitation creation is an internal application operation only.</p>
 */
@Configuration
public class TeamConfig {

    @Bean
    @Profile("test")
    InvitationRepository inMemoryInvitationRepository() {
        return new InMemoryInvitationRepository();
    }

    @Bean
    InvitationTokenFactory invitationTokenFactory() {
        return new SecureInvitationTokenFactory();
    }

    @Bean
    InvitationService invitationService(InvitationRepository invitations, OrganizationRepository organizations,
                                        UserRepository users, InvitationTokenFactory tokenFactory, Clock clock) {
        return new InvitationService(invitations, organizations, users, tokenFactory, clock);
    }
}