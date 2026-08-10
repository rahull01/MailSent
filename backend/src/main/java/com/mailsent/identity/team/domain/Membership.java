package com.mailsent.identity.team.domain;

import java.time.Instant;
import java.util.UUID;

import com.mailsent.identity.authentication.domain.Role;

/**
 * Membership — an explicit Organization membership of a User (TEAM-001).
 *
 * <p>Membership is a SEPARATE concept from a User's single immutable home Organization
 * ({@code User.organizationId}, ORG-002 / D6): a User keeps exactly one home Organization
 * and may additionally hold zero or more explicit Memberships in other Organizations.
 * Accepting an invitation creates a Membership but never mutates the User's home
 * Organization.</p>
 *
 * <p>A Membership references an existing {@code User} and an existing {@code Organization}
 * (enforced by foreign keys at the schema level), carries a granted {@link Role}, and is
 * never duplicated: a user has at most one active membership in a given Organization
 * (global {{@code UNIQUE(user_id, organization_id)}} backstop).</p>
 */
public final class Membership {

    private final String id;
    private final String userId;
    private final String organizationId;
    private final Role role;
    private final Instant createdAt;

    private Membership(String id, String userId, String organizationId, Role role, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Creates a membership between an existing user and an existing organization.
     *
     * @param userId         the member's stable identity, required
     * @param organizationId the Organization joined, required
     * @param role           the granted role, required
     * @param createdAt      the instant of establishment, required
     */
    public static Membership create(String userId, String organizationId, Role role, Instant createdAt) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("A user id is required for a membership.");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("An organization id is required for a membership.");
        }
        if (role == null) {
            throw new IllegalArgumentException("A role is required for a membership.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("A creation time is required for a membership.");
        }
        return new Membership(UUID.randomUUID().toString(), userId, organizationId, role, createdAt);
    }

    /**
     * Reconstructs a membership from durable storage, adopting its stored state verbatim.
     */
    public static Membership restore(String id, String userId, String organizationId, Role role,
                                     Instant createdAt) {
        return new Membership(id, userId, organizationId, role, createdAt);
    }

    public String id() {
        return id;
    }

    /** The member user's stable identity. */
    public String userId() {
        return userId;
    }

    /** The Organization joined. */
    public String organizationId() {
        return organizationId;
    }

    /** The granted role. */
    public Role role() {
        return role;
    }

    /** The instant the membership was established. */
    public Instant createdAt() {
        return createdAt;
    }
}