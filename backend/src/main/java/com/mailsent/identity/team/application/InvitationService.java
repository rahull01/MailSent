package com.mailsent.identity.team.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Pattern;

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
import com.mailsent.identity.team.domain.MalformedInvitationTokenException;
import com.mailsent.identity.team.domain.OrganizationMismatchException;
import com.mailsent.identity.team.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * InvitationService — the TEAM-001 application boundary (frozen contract v8).
 *
 * <p><strong>Creation</strong> ({@link #invite}) is an internal application-layer operation
 * with no externally reachable HTTP surface and no authorization (deferred to AUTH-006 /
 * TEAM-002). It validates the target organization exists, reuses the existing
 * {@code Email}/role contracts, hashes a secure acceptance token (only the hash is
 * persisted), and persists a PENDING invitation. Duplicate PENDING invitations for the
 * exact {@code (organizationId, canonical inviteeEmail)} pair are prevented race-safely by
 * the database partial unique index, whose violation is translated to
 * {@code PendingInvitationAlreadyExistsException} by the persistence adapter.</p>
 *
 * <p><strong>Acceptance</strong> ({@link #accept}) executes inside ONE application
 * transaction in the frozen order: validate token format → hash → resolve by tokenHash →
 * already-accepted decision → resolve existing user by persisted canonical email →
 * validate home-organization equality → {@code markAcceptedIfPending()} → interpret the
 * affected-row count. Acceptance creates NO membership and performs NO other side effect;
 * it never reassigns a home organization. The inviter identity is intentionally absent
 * (deferred to AUTH-006).</p>
 */
public class InvitationService {

    /** The exact raw token representation: 64 lowercase ASCII hexadecimal characters. */
    private static final Pattern TOKEN = Pattern.compile("^[0-9a-f]{64}$");

    private final InvitationRepository invitations;
    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final InvitationTokenFactory tokenFactory;
    private final Clock clock;

    public InvitationService(InvitationRepository invitations, OrganizationRepository organizations,
                             UserRepository users, InvitationTokenFactory tokenFactory, Clock clock) {
        this.invitations = invitations;
        this.organizations = organizations;
        this.users = users;
        this.tokenFactory = tokenFactory;
        this.clock = clock;
    }

    /**
     * Creates a PENDING invitation (internal application-layer operation — no HTTP surface).
     *
     * @param organizationId the target Organization's stable identity
     * @param inviteeEmail   the email invited (the existing approved {@link Email} contract)
     * @param roleName       one of OWNER / ANALYST / VIEWER (data-only)
     * @return the created invitation id plus the raw acceptance token (returned exactly once)
     * @throws OrganizationNotFoundException if the target organization does not exist
     * @throws IllegalArgumentException      if the role is not a canonical role
     */
    @Transactional
    public InvitationCreationResult invite(String organizationId, String inviteeEmail, String roleName) {
        organizations.findById(organizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        Email email = new Email(inviteeEmail);
        Role role = requireCanonicalRole(roleName);

        InvitationToken token = tokenFactory.randomToken();
        Invitation invitation = Invitation.register(organizationId, email, role, token.tokenHash(),
            Instant.now(clock));
        Invitation saved = invitations.save(invitation);
        return new InvitationCreationResult(saved.id(), token.rawToken());
    }

    /**
     * Accepts an invitation (frozen v8 §8 order), inside ONE application transaction.
     *
     * @param rawToken the raw acceptance token presented by the invitee
     * @return the accepted {@link Invitation}
     * @throws MalformedInvitationTokenException if the token is not exactly 64 lowercase hex
     * @throws InvalidInvitationTokenException   if the token resolves to no invitation
     * @throws InvitationAlreadyAcceptedException if already accepted (single-use)
     * @throws InviteeNotFoundException         if the invited email has no existing account
     * @throws OrganizationMismatchException    if the invitation's org != the invitee's home org
     */
    @Transactional
    public Invitation accept(String rawToken) {
        if (rawToken == null || !TOKEN.matcher(rawToken).matches()) {
            // Frozen input validation: reject a malformed token BEFORE any database lookup.
            throw new MalformedInvitationTokenException();
        }

        String tokenHash = tokenFactory.hash(rawToken);
        Invitation invitation = invitations.findByTokenHash(tokenHash)
            .orElseThrow(InvalidInvitationTokenException::new);

        if (invitation.isAccepted()) {
            // Already-accepted decision is made here, before account/org checks, so a later
            // step can never override it with ACCOUNT_REQUIRED / ORG_MISMATCH.
            throw new InvitationAlreadyAcceptedException();
        }

        User invitee = users.findByEmail(invitation.inviteeEmail())
            .orElseThrow(() -> new InviteeNotFoundException(invitation.inviteeEmail().value()));

        if (!invitation.organizationId().equals(invitee.organizationId())) {
            throw new OrganizationMismatchException();
        }

        Instant acceptedAt = Instant.now(clock);
        int affectedRows = invitations.markAcceptedIfPending(invitation.id(), acceptedAt);
        if (affectedRows == 0) {
            // A concurrent/late acceptance already transitioned this invitation.
            throw new InvitationAlreadyAcceptedException();
        }
        // Reflect the database transition on the returned aggregate so callers observe the
        // ACCEPTED lifecycle state they just caused.
        invitation.accept(acceptedAt);
        return invitation;
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