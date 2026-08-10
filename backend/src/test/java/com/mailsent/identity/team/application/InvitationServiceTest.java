package com.mailsent.identity.team.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

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
import com.mailsent.identity.team.domain.MalformedInvitationTokenException;
import com.mailsent.identity.team.domain.OrganizationMismatchException;
import com.mailsent.identity.team.domain.OrganizationNotFoundException;
import com.mailsent.identity.team.infrastructure.InMemoryInvitationRepository;
import com.mailsent.identity.team.infrastructure.SecureInvitationTokenFactory;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link InvitationService} (TEAM-001 v8) against the hermetic in-memory
 * repositories — covering the frozen unit/hermetic acceptance-criteria list without
 * duplication. Real-PostgreSQL invariants (partial unique index, concurrency) are covered
 * in the TEAM-001 integration class.
 */
class InvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);
    private static final Pattern TOKEN = Pattern.compile("^[0-9a-f]{64}$");

    private InMemoryOrganizationRepository organizations;
    private InMemoryUserRepository users;
    private InMemoryInvitationRepository invitations;
    private InvitationService service;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        users = new InMemoryUserRepository();
        invitations = new InMemoryInvitationRepository();
        service = new InvitationService(invitations, organizations, users,
            new SecureInvitationTokenFactory(), CLOCK);
    }

    private String createdOrganization() {
        return organizations.save(Organization.create("Acme", CLOCK.instant())).id();
    }

    private void createdUser(String email, String homeOrgId) {
        users.save(User.register(new Email(email), "pbkdf2$hash", CLOCK.instant(), homeOrgId));
    }

    @Test
    void invitePersistsPendingInvitationAndReturnsRawTokenOnce() {
        String orgId = createdOrganization();
        InvitationCreationResult result = service.invite(orgId, "member@example.com", "VIEWER");

        assertTrue(TOKEN.matcher(result.rawToken()).matches(), "raw token is 64 lowercase hex");
        assertNotNull(result.invitationId());
        Invitation persisted = invitations.findByTokenHash(
            new SecureInvitationTokenFactory().hash(result.rawToken())).orElseThrow();
        assertEquals(Invitation.Status.PENDING, persisted.status());
        assertEquals(orgId, persisted.organizationId());
        assertEquals(orgId, result.invitationId().isEmpty() ? orgId : persisted.organizationId());
    }

    @Test
    void inviteRejectsUnknownOrganization() {
        assertThrows(OrganizationNotFoundException.class,
            () -> service.invite("no-such-org", "member@example.com", "VIEWER"));
    }

    @Test
    void inviteReusesExactCanonicalEmail() {
        String orgId = createdOrganization();
        InvitationCreationResult result = service.invite(orgId, "Member@Example.COM", "VIEWER");
        Invitation persisted = invitations.findByTokenHash(
            new SecureInvitationTokenFactory().hash(result.rawToken())).orElseThrow();
        assertEquals("member@example.com", persisted.inviteeEmail().value(),
            "the persisted value is the exact canonical Email, no additional normalization");
    }

    @Test
    void inviteAcceptsOnlyCanonicalRoleSet() {
        String orgId = createdOrganization();
        service.invite(orgId, "owner@example.com", "OWNER");
        assertThrows(IllegalArgumentException.class,
            () -> service.invite(orgId, "x@example.com", "SUPERADMIN"));
    }

    @Test
    void tokenHashIsExactLowercaseSha256AndRawTokenIsNotStored() {
        String orgId = createdOrganization();
        InvitationCreationResult result = service.invite(orgId, "member@example.com", "VIEWER");
        Invitation persisted = invitations.findByTokenHash(
            new SecureInvitationTokenFactory().hash(result.rawToken())).orElseThrow();
        assertTrue(TOKEN.matcher(persisted.tokenHash()).matches(), "tokenHash is 64 lowercase hex");
        assertFalse(persisted.tokenHash().equals(result.rawToken()), "raw token is never persisted");
    }

    @Test
    void acceptExistingAccountSameHomeOrgSucceedsWithoutSideEffects() {
        String homeOrgId = createdOrganization();
        createdUser("member@example.com", homeOrgId);
        InvitationCreationResult result = service.invite(homeOrgId, "member@example.com", "ANALYST");

        Invitation accepted = service.accept(result.rawToken());

        assertTrue(accepted.isAccepted());
        assertNotNull(accepted.acceptedAt(), "acceptedAt is populated on acceptance");
        // No membership, no home-org change, no authorization/scope effect: the user keeps its
        // single immutable home org and no new aggregate is created.
        assertEquals(homeOrgId, users.findByEmail(new Email("member@example.com")).orElseThrow().organizationId(),
            "home organization is unchanged by acceptance");
    }

    @Test
    void acceptDifferentHomeOrgIsRejected() {
        String homeOrgId = createdOrganization();
        String otherOrgId = createdOrganization();
        createdUser("member@example.com", homeOrgId);
        InvitationCreationResult result = service.invite(otherOrgId, "member@example.com", "VIEWER");

        assertThrows(OrganizationMismatchException.class, () -> service.accept(result.rawToken()));
    }

    @Test
    void acceptMissingAccountIsRejected() {
        String orgId = createdOrganization();
        InvitationCreationResult result = service.invite(orgId, "nobody@example.com", "VIEWER");
        assertThrows(InviteeNotFoundException.class, () -> service.accept(result.rawToken()));
    }

    @Test
    void acceptUnknownTokenIsRejected() {
        assertThrows(InvalidInvitationTokenException.class,
            () -> service.accept("0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    void acceptAlreadyAcceptedTokenIsRejected() {
        String homeOrgId = createdOrganization();
        createdUser("member@example.com", homeOrgId);
        InvitationCreationResult result = service.invite(homeOrgId, "member@example.com", "VIEWER");

        service.accept(result.rawToken());
        assertThrows(InvitationAlreadyAcceptedException.class, () -> service.accept(result.rawToken()));
    }

    @Test
    void alreadyAcceptedDecisionComesBeforeOrgChecks() {
        // An ACCEPTED invitation whose invitee has a DIFFERENT home org must yield the
        // already-accepted error (not ORG_MISMATCH), proving the ordering of step 4.
        String homeOrgId = createdOrganization();
        String otherOrgId = createdOrganization();
        createdUser("member@example.com", homeOrgId);
        InvitationCreationResult result = service.invite(otherOrgId, "member@example.com", "VIEWER");
        // Force ACCEPTED via the in-memory conditional transition (no org check at this level).
        assertEquals(1, invitations.markAcceptedIfPending(result.invitationId(), CLOCK.instant()));

        assertThrows(InvitationAlreadyAcceptedException.class, () -> service.accept(result.rawToken()));
    }

    @Test
    void acceptMalformedTokenIsRejectedBeforeDatabaseLookup() {
        InMemoryInvitationRepository repo = spy(new InMemoryInvitationRepository());
        InvitationService svc = new InvitationService(repo, organizations, users,
            new SecureInvitationTokenFactory(), CLOCK);

        assertThrows(MalformedInvitationTokenException.class, () -> svc.accept("not-a-valid-token"));
        verify(repo, never()).findByTokenHash(anyString());
    }

    @Test
    void acceptAffectedRowsZeroYieldsAlreadyAccepted() {
        // Simulate a concurrent-losing acceptance: the atomic conditional update reports 0
        // affected rows, so acceptance must fail as already-accepted with no side effect.
        String homeOrgId = createdOrganization();
        createdUser("member@example.com", homeOrgId);
        InvitationCreationResult result = service.invite(homeOrgId, "member@example.com", "VIEWER");

        InMemoryInvitationRepository repo = spy(new InMemoryInvitationRepository());
        // Seed the invitation into the spy'd repo so lookup succeeds.
        Invitation pending = Invitation.restore(result.invitationId(), homeOrgId,
            new Email("member@example.com"), Role.VIEWER, new SecureInvitationTokenFactory().hash(result.rawToken()),
            Invitation.Status.PENDING, CLOCK.instant(), null);
        repo.save(pending);
        when(repo.markAcceptedIfPending(anyString(), any())).thenReturn(0);

        InvitationService svc = new InvitationService(repo, organizations, users,
            new SecureInvitationTokenFactory(), CLOCK);
        assertThrows(InvitationAlreadyAcceptedException.class, () -> svc.accept(result.rawToken()));
        assertFalse(pending.isAccepted(), "a failed acceptance changes no state");
    }
}