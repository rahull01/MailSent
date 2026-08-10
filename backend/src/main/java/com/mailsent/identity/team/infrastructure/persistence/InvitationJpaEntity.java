package com.mailsent.identity.team.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.team.domain.Invitation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * InvitationJpaEntity — durable persistence mapping for the {@link Invitation} aggregate.
 * A persistence concern, not a duplicate domain model. Stores the target organization, the
 * canonical invited email, the data-only role, the token hash (never the raw token), and the
 * lifecycle state ({@code accepted_at} is NULL while PENDING). The {@code uk_invitations_token_hash}
 * UNIQUE constraint mirrors V6 for token identity; the duplicate-PENDING invariant is the
 * PENDING-scoped partial unique index in V6 (not a JPA-mapped constraint). Single-use is
 * enforced by the atomic conditional UPDATE in {@code JpaInvitationRepository}, not here.
 */
@Entity
@Table(name = "invitations",
    uniqueConstraints = @UniqueConstraint(name = "uk_invitations_token_hash", columnNames = "token_hash"))
public class InvitationJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "invitee_email", length = 254, nullable = false)
    private String inviteeEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 16, nullable = false)
    private Role role;

    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private Invitation.Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    protected InvitationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public InvitationJpaEntity(String id, String organizationId, String inviteeEmail, Role role,
                               String tokenHash, Invitation.Status status, Instant createdAt, Instant acceptedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.inviteeEmail = inviteeEmail;
        this.role = role;
        this.tokenHash = tokenHash;
        this.status = status;
        this.createdAt = createdAt;
        this.acceptedAt = acceptedAt;
    }

    public String id() {
        return id;
    }

    public String organizationId() {
        return organizationId;
    }

    public String inviteeEmail() {
        return inviteeEmail;
    }

    public Role role() {
        return role;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Invitation.Status status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }
}