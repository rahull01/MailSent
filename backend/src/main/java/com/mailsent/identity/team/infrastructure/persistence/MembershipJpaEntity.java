package com.mailsent.identity.team.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.team.domain.Membership;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * MembershipJpaEntity — durable persistence mapping for the {@link Membership} aggregate.
 * A persistence concern, not a duplicate domain model. Stores the member user, the joined
 * organization, and the granted role. The {@code uk_memberships_user_organization} unique
 * constraint mirrors V6 as the race-safe backstop against duplicate active membership. The
 * owning-user and owning-organization FKs are enforced at the schema level (INV-14).
 */
@Entity
@Table(name = "memberships",
    uniqueConstraints = @UniqueConstraint(name = "uk_memberships_user_organization",
        columnNames = {"user_id", "organization_id"}))
public class MembershipJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 16, nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MembershipJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public MembershipJpaEntity(String id, String userId, String organizationId, Role role, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String organizationId() {
        return organizationId;
    }

    public Role role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }
}