package com.mailsent.identity.authentication.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.identity.authentication.domain.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * UserJpaEntity — the durable persistence mapping for the {@code User} aggregate.
 *
 * <p><strong>This is a persistence concern, not a duplicate domain model.</strong> The
 * domain aggregate is {@code com.mailsent.identity.authentication.domain.User}; this
 * entity is the storage shape the infrastructure adapter maps to and from. It exists
 * so the application layer owns a {@code UserRepository} port that production satisfies
 * with PostgreSQL while tests keep a hermetic adapter.</p>
 *
 * <p>The email column carries a database unique constraint — this is the authoritative
 * guard against duplicate identities under concurrency. The {@code existsByEmail}
 * pre-check in the use case is a fast-path; the constraint is the race-safe backstop
 * (INV-10 stable identity).</p>
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class UserJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "email", length = 254, nullable = false)
    private String email;

    /** The irreversible hash; the raw password is never persisted. */
    @Column(name = "password_hash", length = 512, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 16, nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * The home Organization this user belongs to (exactly one, INV-09 / D6). Nullable at
     * the schema level so the additive V4 migration is compatible with any pre-existing
     * rows; a new signup always provides a valid value (see SignUpService).
     */
    @Column(name = "organization_id", length = 36, nullable = true)
    private String organizationId;

    protected UserJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public UserJpaEntity(String id, String email, String passwordHash, Role role, Instant createdAt,
                         String organizationId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
        this.organizationId = organizationId;
    }

    public String id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String organizationId() {
        return organizationId;
    }
}