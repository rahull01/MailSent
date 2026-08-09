package com.mailsent.identity.authentication.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User} — the identity aggregate created at sign up.
 */
class UserTest {

    @Test
    void registrationCreatesStableIdentityWithDefaultRole() {
        Instant now = Instant.parse("2026-08-07T10:00:00Z");
        User user = User.register(new Email("owner@example.com"), "hash", now);

        assertNotNull(user.id());
        assertEquals("owner@example.com", user.email().value());
        assertEquals("hash", user.passwordHash());
        assertEquals(Role.VIEWER, user.role());
        assertEquals(now, user.createdAt());
    }

    @Test
    void eachRegistrationGetsItsOwnIdentity() {
        Instant now = Instant.now();
        User a = User.register(new Email("a@example.com"), "h1", now);
        User b = User.register(new Email("b@example.com"), "h2", now);
        assertTrue(!a.id().equals(b.id()), "no two users share one identity (INV-10)");
    }

    @Test
    void restorePreservesTheAssignedIdentityAndRole() {
        User original = User.register(new Email("owner@example.com"), "hash", Instant.now());
        User restored = User.restore(original.id(), new Email("owner@example.com"), "hash", Role.ANALYST, original.createdAt());

        assertEquals(original.id(), restored.id(), "restore keeps the stable identity");
        assertEquals(Role.ANALYST, restored.role(), "restore adopts the stored role, never reassigning");
    }
}