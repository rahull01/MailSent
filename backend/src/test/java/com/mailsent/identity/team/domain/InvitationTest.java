package com.mailsent.identity.team.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;

/**
 * Unit tests for the {@link Invitation} aggregate (TEAM-001) — required fields, PENDING
 * creation, and the single-use PENDING→ACCEPTED transition.
 */
class InvitationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String ORG = "org-1";

    @Test
    void registerCreatesAPendingInvitation() {
        Invitation invitation = Invitation.register(ORG, new Email("member@example.com"),
            Role.VIEWER, "abc123", NOW);
        assertEquals(Invitation.Status.PENDING, invitation.status());
        assertFalse(invitation.isAccepted());
        assertEquals(ORG, invitation.organizationId());
        assertEquals("member@example.com", invitation.inviteeEmail().value());
        assertEquals(Role.VIEWER, invitation.role());
    }

    @Test
    void registerRequiresOrganization() {
        assertThrows(IllegalArgumentException.class,
            () -> Invitation.register(null, new Email("m@example.com"), Role.VIEWER, "h", NOW));
    }

    @Test
    void registerRequiresInviteeEmail() {
        assertThrows(IllegalArgumentException.class,
            () -> Invitation.register(ORG, null, Role.VIEWER, "h", NOW));
    }

    @Test
    void registerRequiresRole() {
        assertThrows(IllegalArgumentException.class,
            () -> Invitation.register(ORG, new Email("m@example.com"), null, "h", NOW));
    }

    @Test
    void registerRequiresTokenHash() {
        assertThrows(IllegalArgumentException.class,
            () -> Invitation.register(ORG, new Email("m@example.com"), Role.VIEWER, null, NOW));
    }

    @Test
    void acceptTransitionsPendingToAcceptedExactlyOnce() {
        Invitation invitation = Invitation.register(ORG, new Email("m@example.com"), Role.VIEWER, "h", NOW);
        invitation.accept();
        assertTrue(invitation.isAccepted());
        // accept() on an accepted invitation is a no-op (single-use enforced at the service).
        invitation.accept();
        assertTrue(invitation.isAccepted());
    }

    @Test
    void restoreAdoptsStoredStateVerbatim() {
        Invitation invitation = Invitation.restore("inv-1", ORG, new Email("m@example.com"), Role.OWNER,
            "hash", Invitation.Status.ACCEPTED, NOW);
        assertEquals("inv-1", invitation.id());
        assertTrue(invitation.isAccepted());
        assertEquals(Role.OWNER, invitation.role());
    }
}