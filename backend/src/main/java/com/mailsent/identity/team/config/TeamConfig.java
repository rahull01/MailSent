package com.mailsent.identity.team.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.application.InvitationService;
import com.mailsent.identity.team.application.MembershipRepository;
import com.mailsent.identity.team.domain.InvitationTokenFactory;
import com.mailsent.identity.team.infrastructure.InMemoryInvitationRepository;
import com.mailsent.identity.team.infrastructure.InMemoryMembershipRepository;
import com.mailsent.identity.team.infrastructure.SecureInvitationTokenFactory;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * Configuration and dependency wiring for the team module (TEAM-001, Identity context).
 *
 * <p>There is exactly one {@link InvitationRepository} and one {@link MembershipRepository}
 * per profile: the PostgreSQL adapters ({@code JpaInvitationRepositoryAdapter} and
 * {@code JpaMembershipRepositoryAdapter}) are component-scanned under {@code !test}; the
 * hermetic in-memory repositories are declared here under the {@code test} profile. The
 * {@link InvitationService} completes the wiring, taking the shared
 * {@link OrganizationRepository}, {@link UserRepository}, {@link Clock}, and a secure
 * {@link InvitationTokenFactory}.</p>
 *
 * <p>TEAM-001 exposes a single HTTP accept surface ({@code InvitationAcceptController});
 * invitation creation is an application operation only.</p>
 */
@Configuration
public class TeamConfig {

    @Bean
    @Profile("test")
    InvitationRepository inMemoryInvitationRepository() {
        return new InMemoryInvitationRepository();
    }

    @Bean
    @Profile("test")
    MembershipRepository inMemoryMembershipRepository() {
        return new InMemoryMembershipRepository();
    }

    @Bean
    InvitationTokenFactory invitationTokenFactory() {
        return new SecureInvitationTokenFactory();
    }

    @Bean
    InvitationService invitationService(InvitationRepository invitations, MembershipRepository memberships,
                                        OrganizationRepository organizations, UserRepository users,
                                        InvitationTokenFactory tokenFactory, Clock clock) {
        return new InvitationService(invitations, memberships, organizations, users, tokenFactory, clock);
    }
}