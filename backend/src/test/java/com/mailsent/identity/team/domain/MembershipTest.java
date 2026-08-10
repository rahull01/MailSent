package com.mailsent.identity.team.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Role;

/**
 * Unit tests for the {@link Membership} aggregate (TEAM-001) — required references.
 */
class MembershipTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String USER = "user-1";
    private static final String ORG = "org-1";

    @Test
    void createBindsUserToOrganizationWithRole() {
        Membership membership = Membership.create(USER, ORG, Role.ANALYST, NOW);
        assertEquals(USER, membership.userId());
        assertEquals(ORG, membership.organizationId());
        assertEquals(Role.ANALYST, membership.role());
    }

    @Test
    void createRequiresUser() {
        assertThrows(IllegalArgumentException.class, () -> Membership.create(null, ORG, Role.VIEWER, NOW));
    }

    @Test
    void createRequiresOrganization() {
        assertThrows(IllegalArgumentException.class, () -> Membership.create(USER, null, Role.VIEWER, NOW));
    }

    @Test
    void createRequiresRole() {
        assertThrows(IllegalArgumentException.class, () -> Membership.create(USER, ORG, null, NOW));
    }

    @Test
    void createRequiresCreatedAt() {
        assertThrows(IllegalArgumentException.class, () -> Membership.create(USER, ORG, Role.VIEWER, null));
    }
}