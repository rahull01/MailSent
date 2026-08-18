package com.mailsent.identity.team.application;

import java.time.Instant;
import java.util.Objects;

import com.mailsent.identity.authentication.domain.Role;

/**
 * TeamMemberSummary — Safe DTO representing an active member in an organization (TEAM-004).
 *
 * <p>Contains only safe public attributes; passwordHash and credentials are strictly excluded (N4 Zero Secrets).</p>
 */
public record TeamMemberSummary(
        String userId,
        String email,
        Role role,
        Instant createdAt
) {
    public TeamMemberSummary {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
