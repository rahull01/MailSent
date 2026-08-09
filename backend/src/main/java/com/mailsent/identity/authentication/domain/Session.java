package com.mailsent.identity.authentication.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Session — a hardened, time-boxed authentication for an authenticated {@link User}.
 *
 * <p>A session is created atomically at sign-in, holds a stable identity, and is
 * authenticated by a high-entropy token whose hash — and only the hash — is persisted.
 * It expires, so a leaked or idle credential cannot authenticate indefinitely. It is a
 * value the Identity and Access context owns: it answers "who is this caller and may
 * they act now".</p>
 *
 * <p><strong>Security decisions:</strong> the raw token is returned to the caller exactly
 * once and never stored or logged; only {@link SessionToken#tokenHash()} is saved. The
 * session has a bounded lifetime (NFR-S2); expiry is enforced by the holder
 * ({@link #isActiveAt(Instant)}).</p>
 */
public final class Session {

    private final String id;
    private final String userId;
    private final String tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;

    private Session(String id, String userId, String tokenHash, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Creates a new session bound to a user, issued at {@code now}.
     *
     * @param userId   the authenticated user's stable identity
     * @param token    the freshly generated token (raw + stored hash)
     * @param now      the instant the session is issued
     * @param lifetime how long the session is valid
     */
    public static Session issue(String userId, SessionToken token, Instant now, Duration lifetime) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("A user identity is required to issue a session.");
        }
        if (token == null) {
            throw new IllegalArgumentException("A session token is required.");
        }
        if (lifetime == null || lifetime.isNegative() || lifetime.isZero()) {
            throw new IllegalArgumentException("A session lifetime must be positive.");
        }
        return new Session(UUID.randomUUID().toString(), userId, token.tokenHash(), now, now.plus(lifetime));
    }

    /**
     * Reconstructs a session from durable storage (persistence hydration, never reassigns
     * identity). </p>
     */
    public static Session restore(String id, String userId, String tokenHash, Instant createdAt, Instant expiresAt) {
        return new Session(id, userId, tokenHash, createdAt, expiresAt);
    }

    /**
     * Whether the session is still valid at {@code now} (not yet expired).
     */
    public boolean isActive(Instant now) {
        return !expiresAt.isBefore(now);
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    /** The stored hash of the bearer token; the raw token is never persisted. */
    public String tokenHash() {
        return tokenHash;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}