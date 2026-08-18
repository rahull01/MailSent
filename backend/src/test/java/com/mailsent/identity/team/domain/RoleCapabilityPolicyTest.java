package com.mailsent.identity.team.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Role;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleCapabilityPolicy Specification Tests (TEAM-002)")
class RoleCapabilityPolicyTest {

    @Test
    @DisplayName("OWNER has all 13 defined capabilities")
    void ownerHasAllCapabilities() {
        Set<Capability> ownerCaps = RoleCapabilityPolicy.getCapabilitiesForRole(Role.OWNER);
        assertEquals(13, ownerCaps.size());
        for (Capability cap : Capability.values()) {
            assertTrue(RoleCapabilityPolicy.hasCapability(Role.OWNER, cap), "OWNER must have " + cap);
        }
    }

    @Test
    @DisplayName("ANALYST has read and operational mutation capabilities, but no admin/team/billing capabilities")
    void analystCapabilityBoundaries() {
        Set<Capability> analystCaps = RoleCapabilityPolicy.getCapabilitiesForRole(Role.ANALYST);
        assertEquals(7, analystCaps.size());

        // Allowed
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.HEALTH_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.DIAGNOSIS_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.REPORT_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.READINESS_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.MAP_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.DOMAIN_OBSERVE_TRIGGER));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.RESOLUTION_VERIFY));

        // Denied (administrative, team, billing, registry mutations)
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.DOMAIN_MANAGE));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.CONSENT_GRANT));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.TEAM_INVITE));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.TEAM_MANAGE_ROLES));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.ORG_SETTINGS_MANAGE));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.ANALYST, Capability.BILLING_MANAGE));
    }

    @Test
    @DisplayName("VIEWER has read capabilities only (least authority default)")
    void viewerCapabilityBoundaries() {
        Set<Capability> viewerCaps = RoleCapabilityPolicy.getCapabilitiesForRole(Role.VIEWER);
        assertEquals(5, viewerCaps.size());

        // Allowed read views
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.HEALTH_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.DIAGNOSIS_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.REPORT_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.READINESS_VIEW));
        assertTrue(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.MAP_VIEW));

        // Denied all mutations and operational actions
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.DOMAIN_OBSERVE_TRIGGER));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.RESOLUTION_VERIFY));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.DOMAIN_MANAGE));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.CONSENT_GRANT));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.TEAM_INVITE));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.TEAM_MANAGE_ROLES));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.ORG_SETTINGS_MANAGE));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.VIEWER, Capability.BILLING_MANAGE));
    }

    @Test
    @DisplayName("Default-deny: null inputs return false and empty set")
    void defaultDenyOnNulls() {
        assertFalse(RoleCapabilityPolicy.hasCapability(null, Capability.HEALTH_VIEW));
        assertFalse(RoleCapabilityPolicy.hasCapability(Role.OWNER, null));
        assertFalse(RoleCapabilityPolicy.hasCapability(null, null));
        assertTrue(RoleCapabilityPolicy.getCapabilitiesForRole(null).isEmpty());
    }
}
