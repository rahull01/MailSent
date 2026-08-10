package com.mailsent.identity.team.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;

/**
 * Unit tests for the {@link Invitation} aggregate (TEAM-001 v8) — required fields, PENDING
 * creation with {@code acceptedAt = null}, and the lifecycle {@code accept(Instant)} rule.
 */
class InvitationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String ORG = "org-1";

    @Test
    void registerCreatesPendingInvitationWithNullAcceptedAt() {
        Invitation invitation = Invitation.register(ORG, new Email("m@example.com"), Role.VIEWER, "abc123", NOW);
        assertEquals(Invitation.Status.PENDING, invitation.status());
        assertFalse(invitation.isAccepted());
        assertNull(invitation.acceptedAt());
        assertEquals(ORG, invitation.organizationId());
        assertEquals("m@example.com", invitation.inviteeEmail().value());
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
    void acceptTransitionsPendingToAcceptedAndRecordsAcceptedAt() {
        Invitation invitation = Invitation.register(ORG, new Email("m@example.com"), Role.VIEWER, "h", NOW);
        assertTrue(invitation.accept(NOW.plusSeconds(5)));
        assertTrue(invitation.isAccepted());
        assertEquals(NOW.plusSeconds(5), invitation.acceptedAt());
        // Re-accept is a no-op: lifecycle stops at ACCEPTED.
        assertFalse(invitation.accept(NOW.plusSeconds(10)));
        assertEquals(NOW.plusSeconds(5), invitation.acceptedAt());
    }

    @Test
    void restoreAdoptsStoredStateVerbatim() {
        Invitation invitation = Invitation.restore("inv-1", ORG, new Email("m@example.com"), Role.OWNER,
            "hash", Invitation.Status.ACCEPTED, NOW, NOW.plusSeconds(9));
        assertEquals("inv-1", invitation.id());
        assertTrue(invitation.isAccepted());
        assertEquals(Role.OWNER, invitation.role());
        assertEquals(NOW.plusSeconds(9), invitation.acceptedAt());
    }
}