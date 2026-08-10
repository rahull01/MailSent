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
 * invited email, the granted role, the token hash (never the raw token), and lifecycle
 * state. The {@code uk_invitations_token_hash} unique constraint mirrors V6 so the token
 * resolves to at most one invitation and its hash is never duplicated.
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

    protected InvitationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public InvitationJpaEntity(String id, String organizationId, String inviteeEmail, Role role,
                               String tokenHash, Invitation.Status status, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.inviteeEmail = inviteeEmail;
        this.role = role;
        this.tokenHash = tokenHash;
        this.status = status;
        this.createdAt = createdAt;
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
}