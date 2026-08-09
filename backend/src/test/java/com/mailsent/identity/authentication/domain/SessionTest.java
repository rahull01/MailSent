package com.mailsent.identity.authentication.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Session} — the hardened, time-boxed authentication entity.
 */
class SessionTest {

    private final SessionToken token = new SessionToken("raw-token", "token-hash");

    @Test
    void issueCreatesActiveSessionBoundToUser() {
        Instant now = Instant.parse("2026-08-08T10:00:00Z");
        Session s = Session.issue("user-1", token, now, Duration.ofMinutes(60));

        assertEquals("user-1", s.userId());
        assertEquals("token-hash", s.tokenHash(), "only the hash is stored, never the raw value");
        assertEquals(now, s.createdAt());
        assertEquals(now.plus(Duration.ofMinutes(60)), s.expiresAt());
        assertTrue(s.isActive(now));
        assertTrue(s.isActive(now.plus(Duration.ofMinutes(59))));
    }

    @Test
    void sessionExpiresAtBoundary() {
        Instant now = Instant.parse("2026-08-08T10:00:00Z");
        Session s = Session.issue("user-1", token, now, Duration.ofMinutes(60));
        assertFalse(s.isActive(now.plus(Duration.ofMinutes(61))));
    }

    @Test
    void issueRejectsBlankUser() {
        assertThrows(IllegalArgumentException.class,
            () -> Session.issue("  ", token, Instant.now(), Duration.ofMinutes(1)));
    }

    @Test
    void issueRejectsNullToken() {
        assertThrows(IllegalArgumentException.class,
            () -> Session.issue("user-1", null, Instant.now(), Duration.ofMinutes(1)));
    }

    @Test
    void issueRejectsNonPositiveLifetime() {
        assertThrows(IllegalArgumentException.class,
            () -> Session.issue("user-1", token, Instant.now(), Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
            () -> Session.issue("user-1", token, Instant.now(), Duration.ofMinutes(-1)));
        assertThrows(IllegalArgumentException.class,
            () -> Session.issue("user-1", token, Instant.now(), null));
    }

    @Test
    void restorePreservesStoredSession() {
        Instant now = Instant.parse("2026-08-08T10:00:00Z");
        Session restored = Session.restore("id", "user-1", "hash", now, now.plus(Duration.ofMinutes(30)));
        assertEquals("id", restored.id());
        assertEquals("user-1", restored.userId());
        assertEquals("hash", restored.tokenHash());
        assertEquals(now, restored.createdAt());
        assertEquals(now.plus(Duration.ofMinutes(30)), restored.expiresAt());
    }
}