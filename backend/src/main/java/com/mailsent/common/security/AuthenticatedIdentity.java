package com.mailsent.common.security;

import java.util.Objects;

import com.mailsent.identity.authentication.domain.Role;

/**
 * AuthenticatedIdentity — an immutable value object representing the verified caller.
 *
 * <p>Constructed exclusively by {@link SessionAuthenticationFilter} after validating
 * the session token hash against durable storage and hydrating the corresponding
 * user entity. Bound to {@link TenantContext} for the duration of the request.</p>
 *
 * @param userId         the verified user stable identity (never null or blank)
 * @param email          the canonical email of the authenticated user
 * @param organizationId the home organization identity (INV-09 tenant boundary)
 * @param role           the assigned role (Owner, Analyst, Viewer)
 */
public record AuthenticatedIdentity(
    String userId,
    String email,
    String organizationId,
    Role role
) {

    public AuthenticatedIdentity {
        Objects.requireNonNull(userId, "userId cannot be null");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be blank");
        }
        Objects.requireNonNull(organizationId, "organizationId cannot be null");
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("organizationId cannot be blank");
        }
        Objects.requireNonNull(role, "role cannot be null");
    }

    /**
     * Whether this authenticated caller has permission to make mutations (least authority).
     */
    public boolean canChange() {
        return role.canChange();
    }
}
