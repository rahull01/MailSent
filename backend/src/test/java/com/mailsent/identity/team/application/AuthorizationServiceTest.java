package com.mailsent.identity.team.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.domain.Capability;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorizationService Integration & Security Tests (TEAM-002)")
class AuthorizationServiceTest {

    private AuthorizationService authorizationService;
    private Instant now;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
        now = Instant.now();
    }

    private User createUser(String id, String email, Role role, String orgId) {
        return User.restore(id, new Email(email), "$2a$12$dummyhash", role, now, orgId);
    }

    @Test
    @DisplayName("Owner can execute all operations within their own organization")
    void ownerAuthorizedInHomeOrg() {
        User owner = createUser("usr-owner", "owner@example.com", Role.OWNER, "org-1");

        for (Capability cap : Capability.values()) {
            assertTrue(authorizationService.isAuthorized(owner, "org-1", cap));
            assertDoesNotThrow(() -> authorizationService.checkAuthorization(owner, "org-1", cap));
        }
    }

    @Test
    @DisplayName("Multi-Tenant Isolation (INV-09): Owner of Org A has zero authority in Org B")
    void multiTenantIsolationEnforced() {
        User ownerOrgA = createUser("usr-owner-a", "owner@a.com", Role.OWNER, "org-a");

        for (Capability cap : Capability.values()) {
            assertFalse(authorizationService.isAuthorized(ownerOrgA, "org-b", cap));
            AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                    authorizationService.checkAuthorization(ownerOrgA, "org-b", cap));
            assertEquals("usr-owner-a", ex.userId());
            assertEquals("org-b", ex.organizationId());
            assertEquals(cap, ex.attemptedCapability());
        }
    }

    @Test
    @DisplayName("Analyst can trigger observation and view health, but cannot invite team members")
    void analystAuthorizationRules() {
        User analyst = createUser("usr-analyst", "analyst@example.com", Role.ANALYST, "org-1");

        // Allowed
        assertTrue(authorizationService.isAuthorized(analyst, "org-1", Capability.HEALTH_VIEW));
        assertTrue(authorizationService.isAuthorized(analyst, "org-1", Capability.DOMAIN_OBSERVE_TRIGGER));
        assertTrue(authorizationService.isAuthorized(analyst, "org-1", Capability.RESOLUTION_VERIFY));

        // Denied
        assertFalse(authorizationService.isAuthorized(analyst, "org-1", Capability.TEAM_INVITE));
        assertFalse(authorizationService.isAuthorized(analyst, "org-1", Capability.BILLING_MANAGE));

        assertThrows(AccessDeniedException.class, () ->
                authorizationService.checkAuthorization(analyst, "org-1", Capability.TEAM_INVITE));
    }

    @Test
    @DisplayName("Viewer (least-authority) can only view health, all mutations denied")
    void viewerAuthorizationRules() {
        User viewer = createUser("usr-viewer", "viewer@example.com", Role.VIEWER, "org-1");

        // Allowed
        assertTrue(authorizationService.isAuthorized(viewer, "org-1", Capability.HEALTH_VIEW));
        assertTrue(authorizationService.isAuthorized(viewer, "org-1", Capability.DIAGNOSIS_VIEW));

        // Denied
        assertFalse(authorizationService.isAuthorized(viewer, "org-1", Capability.DOMAIN_OBSERVE_TRIGGER));
        assertFalse(authorizationService.isAuthorized(viewer, "org-1", Capability.RESOLUTION_VERIFY));
        assertFalse(authorizationService.isAuthorized(viewer, "org-1", Capability.DOMAIN_MANAGE));

        assertThrows(AccessDeniedException.class, () ->
                authorizationService.checkAuthorization(viewer, "org-1", Capability.DOMAIN_OBSERVE_TRIGGER));
    }

    @Test
    @DisplayName("Default-deny: Unauthenticated or null user/parameters are denied")
    void defaultDenyOnInvalidInputs() {
        assertFalse(authorizationService.isAuthorized(null, "org-1", Capability.HEALTH_VIEW));
        assertFalse(authorizationService.isAuthorized(createUser("u1", "u1@e.com", Role.OWNER, "org-1"), null, Capability.HEALTH_VIEW));
        assertFalse(authorizationService.isAuthorized(createUser("u1", "u1@e.com", Role.OWNER, "org-1"), "org-1", null));

        assertThrows(AccessDeniedException.class, () ->
                authorizationService.checkAuthorization(null, "org-1", Capability.HEALTH_VIEW));
    }
}
