package com.mailsent.identity.authentication.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * SessionJpaEntity — durable persistence mapping for the {@code Session} aggregate.
 *
 * <p><strong>Persistence concern, not a duplicate domain model.</strong> Stores only the
 * token <em>hash</em> (never the raw bearer value) and the session's time bounds, so a
 * database leak does not disclose usable credentials. A unique constraint on the hash
 * keeps the session keyed and race-safe.</p>
 */
@Entity
@Table(name = "sessions", uniqueConstraints = @UniqueConstraint(name = "uk_sessions_token_hash", columnNames = "token_hash"))
public class SessionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected SessionJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public SessionJpaEntity(String id, String userId, String tokenHash, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

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