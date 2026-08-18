package com.mailsent.portfolio.organization.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * OrgTeamMember — Organization team membership and role configuration (SET-002).
 */
public class OrgTeamMember {

    public enum OrgRole {
        OWNER,
        ADMIN,
        OPERATOR,
        ANALYST,
        VIEWER
    }

    private final String id;
    private final String organizationId;
    private final String userId;
    private final String email;
    private OrgRole role;
    private final Instant joinedAt;

    public OrgTeamMember(String id, String organizationId, String userId, String email, OrgRole role, Instant joinedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.userId = Objects.requireNonNull(userId);
        this.email = Objects.requireNonNull(email);
        this.role = Objects.requireNonNull(role);
        this.joinedAt = Objects.requireNonNull(joinedAt);
    }

    public static OrgTeamMember addMember(String organizationId, String userId, String email, OrgRole role, Instant joinedAt) {
        if (organizationId.isBlank() || userId.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("Fields must not be blank.");
        }
        return new OrgTeamMember(
                UUID.randomUUID().toString(),
                organizationId,
                userId,
                email,
                role,
                joinedAt != null ? joinedAt : Instant.now()
        );
    }

    public void updateRole(OrgRole newRole) {
        this.role = Objects.requireNonNull(newRole);
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String userId() { return userId; }
    public String email() { return email; }
    public OrgRole role() { return role; }
    public Instant joinedAt() { return joinedAt; }
}
