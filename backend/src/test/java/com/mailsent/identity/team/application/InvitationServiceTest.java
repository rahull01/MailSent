package com.mailsent.identity.team.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.domain.InvitationAlreadyAcceptedException;
import com.mailsent.identity.team.domain.InvalidInvitationTokenException;
import com.mailsent.identity.team.domain.InviteeNotFoundException;
import com.mailsent.identity.team.domain.Membership;
import com.mailsent.identity.team.domain.MembershipAlreadyExistsException;
import com.mailsent.identity.team.domain.OrganizationNotFoundException;
import com.mailsent.identity.team.infrastructure.InMemoryInvitationRepository;
import com.mailsent.identity.team.infrastructure.InMemoryMembershipRepository;
import com.mailsent.identity.team.infrastructure.SecureInvitationTokenFactory;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link InvitationService} (TEAM-001) against the hermetic in-memory
 * repositories: invitation creation, acceptance, single-use, duplicate-membership
 * prevention, organization ownership, and home-Organization preservation.
 */
class InvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryUserRepository users;
    private InMemoryInvitationRepository invitations;
    private InMemoryMembershipRepository memberships;
    private InvitationService service;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        users = new InMemoryUserRepository();
        invitations = new InMemoryInvitationRepository();
        memberships = new InMemoryMembershipRepository();
        service = new InvitationService(invitations, memberships, organizations, users,
            new SecureInvitationTokenFactory(), CLOCK);
    }

    private String createdOrganization() {
        return organizations.save(Organization.create("Acme", CLOCK.instant())).id();
    }

    private String createdUser(String email, String homeOrgId) {
        User user = User.register(new Email(email), "pbkdf2$hash", CLOCK.instant(), homeOrgId);
        return users.save(user).id();
    }

    @Test
    void invitePersistsPendingInvitationAndReturnsDeliverableToken() {
        String orgId = createdOrganization();
        InvitationIssued issued = service.invite(orgId, "member@example.com", "VIEWER");

        assertEquals(Invitation.Status.PENDING, issued.invitation().status());
        assertEquals(orgId, issued.invitation().organizationId());
        assertEquals("member@example.com", issued.invitation().inviteeEmail().value());
        assertEquals(Role.VIEWER, issued.invitation().role());
        assertNotNull(issued.rawToken(), "the raw token is returned once for delivery");
        assertTrue(invitations.findByTokenHash(issued.invitation().tokenHash()).isPresent());
    }

    @Test
    void acceptCreatesMembershipAndMarksInvitationAccepted() {
        String orgId = createdOrganization();
        createdUser("member@example.com", createdOrganization());
        InvitationIssued issued = service.invite(orgId, "member@example.com", "ANALYST");

        Membership membership = service.accept(issued.rawToken());

        assertEquals(orgId, membership.organizationId());
        assertEquals(Role.ANALYST, membership.role());
        assertTrue(memberships.existsByUserAndOrganization(membership.userId(), orgId));
        Invitation reloaded = invitations.findByTokenHash(issued.invitation().tokenHash()).orElseThrow();
        assertTrue(reloaded.isAccepted(), "the invitation is accepted (single-use)");
    }

    @Test
    void acceptIsSingleUse() {
        String orgId = createdOrganization();
        createdUser("member@example.com", createdOrganization());
        InvitationIssued issued = service.invite(orgId, "member@example.com", "VIEWER");

        service.accept(issued.rawToken());
        assertThrows(InvitationAlreadyAcceptedException.class, () -> service.accept(issued.rawToken()));
    }

    @Test
    void acceptRejectsUnknownToken() {
        assertThrows(InvalidInvitationTokenException.class, () -> service.accept("not-a-real-token"));
    }

    @Test
    void acceptRejectsInviteeWithoutAccount() {
        String orgId = createdOrganization();
        InvitationIssued issued = service.invite(orgId, "nobody@example.com", "VIEWER");
        assertThrows(InviteeNotFoundException.class, () -> service.accept(issued.rawToken()));
    }

    @Test
    void acceptPreventsDuplicateActiveMembership() {
        String orgId = createdOrganization();
        createdUser("member@example.com", createdOrganization());

        InvitationIssued first = service.invite(orgId, "member@example.com", "VIEWER");
        service.accept(first.rawToken());

        InvitationIssued second = service.invite(orgId, "member@example.com", "ANALYST");
        assertThrows(MembershipAlreadyExistsException.class, () -> service.accept(second.rawToken()));
    }

    @Test
    void inviteRejectsUnknownOrganization() {
        assertThrows(OrganizationNotFoundException.class,
            () -> service.invite("no-such-org", "member@example.com", "VIEWER"));
    }

    @Test
    void inviteRejectsUnknownRole() {
        String orgId = createdOrganization();
        assertThrows(IllegalArgumentException.class,
            () -> service.invite(orgId, "member@example.com", "SUPERADMIN"));
    }

    @Test
    void acceptPreservesTheUsersHomeOrganization() {
        String homeOrgId = createdOrganization();
        String otherOrgId = createdOrganization();
        createdUser("member@example.com", homeOrgId);

        InvitationIssued issued = service.invite(otherOrgId, "member@example.com", "VIEWER");
        Membership membership = service.accept(issued.rawToken());

        assertEquals(otherOrgId, membership.organizationId(), "membership is in the invited organization");
        assertEquals(homeOrgId, users.findByEmail(new Email("member@example.com")).orElseThrow().organizationId(),
            "the user's home organization is unchanged by acceptance (ORG-002 / D6)");
        assertFalse(homeOrgId.equals(otherOrgId), "setup sanity: home and invited orgs differ");
    }
}