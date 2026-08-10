package com.mailsent.identity.team.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

import org.springframework.transaction.annotation.Transactional;

import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.domain.InvitationAlreadyAcceptedException;
import com.mailsent.identity.team.domain.InvitationToken;
import com.mailsent.identity.team.domain.InvitationTokenFactory;
import com.mailsent.identity.team.domain.InviteeNotFoundException;
import com.mailsent.identity.team.domain.InvalidInvitationTokenException;
import com.mailsent.identity.team.domain.Membership;
import com.mailsent.identity.team.domain.MembershipAlreadyExistsException;
import com.mailsent.identity.team.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * InvitationService — the TEAM-001 application boundary: create an invitation and accept
 * it into an explicit Organization membership.
 *
 * <p><strong>Organization-membership foundation.</strong> A User keeps exactly one
 * immutable home Organization (User.organization_id, ORG-002 / D6). A membership is a
 * SEPARATE concept: accepting an invitation creates a new {@link Membership} referencing
 * the target organization and user and never mutates the user's home Organization.</p>
 *
 * <p><strong>Single responsibility / failure mode.</strong> {@link #invite} validates the
 * target organization exists and the role is a canonical AUTH-003 role, hashes a secure
 * acceptance token, and persists a PENDING invitation. {@link #accept} resolves the token,
 * enforces single-use, requires an existing account for the invited email (decision 8
 * existing-user; decision 9 — the new-user/signup path is a separate approved decision and
 * is refused here, not invented), prevents duplicate membership, and creates the membership
 * while marking the invitation accepted in one atomic transaction.</p>
 *
 * <p>The inviter identity is intentionally not modeled: binding an authenticated
 * inviter is the deferred AUTH-006 current-user contract and is not invented here
 * (decision 10).</p>
 */
public class InvitationService {

    private final InvitationRepository invitations;
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final InvitationTokenFactory tokenFactory;
    private final Clock clock;

    public InvitationService(InvitationRepository invitations, MembershipRepository memberships,
                             OrganizationRepository organizations, UserRepository users,
                             InvitationTokenFactory tokenFactory, Clock clock) {
        this.invitations = invitations;
        this.memberships = memberships;
        this.organizations = organizations;
        this.users = users;
        this.tokenFactory = tokenFactory;
        this.clock = clock;
    }

    /**
     * Creates a PENDING invitation for an invitee toward a target Organization.
     *
     * @param organizationId the target Organization's stable identity
     * @param inviteeEmail   the email invited to join
     * @param roleName       the canonical AUTH-003 role name (OWNER / ANALYST / VIEWER)
     * @return the persisted invitation plus the raw acceptance token to be delivered
     * @throws OrganizationNotFoundException if the target organization does not exist
     * @throws IllegalArgumentException      if the role name is not a canonical role
     */
    @Transactional
    public InvitationIssued invite(String organizationId, String inviteeEmail, String roleName) {
        organizations.findById(organizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        Email email = new Email(inviteeEmail);
        Role role = requireCanonicalRole(roleName);

        InvitationToken token = tokenFactory.randomToken();
        Invitation invitation = Invitation.register(organizationId, email, role, token.tokenHash(),
            Instant.now(clock));
        Invitation saved = invitations.save(invitation);
        return new InvitationIssued(saved, token.rawToken());
    }

    /**
     * Accepts an invitation, creating an active membership for the invited user (existing
     * account) in the target Organization and marking the invitation accepted — atomically.
     *
     * @param rawToken the raw acceptance token presented by the invitee
     * @return the created membership
     * @throws InvalidInvitationTokenException if the token resolves to no invitation
     * @throws InvitationAlreadyAcceptedException if the invitation was already accepted (single-use)
     * @throws InviteeNotFoundException if the invited email has no existing account (decision 9)
     * @throws MembershipAlreadyExistsException if the user is already a member of the organization
     */
    @Transactional
    public Membership accept(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidInvitationTokenException();
        }
        String tokenHash = tokenFactory.hash(rawToken);
        Invitation invitation = invitations.findByTokenHash(tokenHash)
            .orElseThrow(InvalidInvitationTokenException::new);
        if (invitation.isAccepted()) {
            throw new InvitationAlreadyAcceptedException();
        }

        User invitee = users.findByEmail(invitation.inviteeEmail())
            .orElseThrow(() -> new InviteeNotFoundException(invitation.inviteeEmail().value()));
        if (memberships.existsByUserAndOrganization(invitee.id(), invitation.organizationId())) {
            throw new MembershipAlreadyExistsException(invitee.id(), invitation.organizationId());
        }

        Membership membership = Membership.create(invitee.id(), invitation.organizationId(),
            invitation.role(), Instant.now(clock));
        invitation.accept();
        memberships.save(membership);
        invitations.save(invitation);
        return membership;
    }

    private Role requireCanonicalRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("A role is required for the invitation.");
        }
        return Arrays.stream(Role.values())
            .filter(r -> r.name().equalsIgnoreCase(roleName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown role '" + roleName + "'; expected one of " + Arrays.toString(Role.values()) + "."));
    }
}