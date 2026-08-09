package com.mailsent.identity.authentication.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User} — the identity aggregate created at sign up.
 */
class UserTest {

    @Test
    void registrationCreatesStableIdentityWithDefaultRoleAndHomeOrganization() {
        Instant now = Instant.parse("2026-08-07T10:00:00Z");
        User user = User.register(new Email("owner@example.com"), "hash", now, "org-1");

        assertNotNull(user.id());
        assertEquals("owner@example.com", user.email().value());
        assertEquals("hash", user.passwordHash());
        assertEquals(Role.VIEWER, user.role());
        assertEquals(now, user.createdAt());
        assertEquals("org-1", user.organizationId(), "a signup user belongs to exactly one home Organization (D6)");
    }

    @Test
    void eachRegistrationGetsItsOwnIdentity() {
        Instant now = Instant.now();
        User a = User.register(new Email("a@example.com"), "h1", now, "org-a");
        User b = User.register(new Email("b@example.com"), "h2", now, "org-b");
        assertTrue(!a.id().equals(b.id()), "no two users share one identity (INV-10)");
    }

    @Test
    void restorePreservesTheAssignedIdentityRoleAndOrganization() {
        User original = User.register(new Email("owner@example.com"), "hash", Instant.now(), "org-1");
        User restored = User.restore(original.id(), new Email("owner@example.com"), "hash",
            Role.ANALYST, original.createdAt(), original.organizationId());

        assertEquals(original.id(), restored.id(), "restore keeps the stable identity");
        assertEquals(Role.ANALYST, restored.role(), "restore adopts the stored role, never reassigning");
        assertEquals("org-1", restored.organizationId(), "restore preserves the home Organization (INV-09 / D6)");
    }

    @Test
    void registrationRejectsMissingOrganization() {
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class,
            () -> User.register(new Email("owner@example.com"), "hash", now, null));
        assertThrows(IllegalArgumentException.class,
            () -> User.register(new Email("owner@example.com"), "hash", now, "  "));
    }
}