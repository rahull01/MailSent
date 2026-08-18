package com.mailsent.identity.team.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mailsent.identity.authentication.domain.Role;

/**
 * RoleCapabilityPolicy — Immutable mapping from canonical Role to authorized Capability set (TEAM-002).
 *
 * <p>Strict Default-Deny: Any Role or Capability not explicitly mapped evaluates to DENY (false).
 * No implicit inheritance is assumed; every permission is explicit per the approved TEAM-002 matrix.</p>
 */
public final class RoleCapabilityPolicy {

    private static final Map<Role, Set<Capability>> MATRIX;

    static {
        Map<Role, Set<Capability>> map = new EnumMap<>(Role.class);

        // VIEWER: Least authority (default for sign-up). Read-only observability across contexts.
        map.put(Role.VIEWER, Collections.unmodifiableSet(EnumSet.of(
                Capability.HEALTH_VIEW,
                Capability.DIAGNOSIS_VIEW,
                Capability.REPORT_VIEW,
                Capability.READINESS_VIEW,
                Capability.MAP_VIEW
        )));

        // ANALYST: Read access + operational mutation (rescan, verify). No admin/org/billing/team mutations.
        map.put(Role.ANALYST, Collections.unmodifiableSet(EnumSet.of(
                Capability.HEALTH_VIEW,
                Capability.DIAGNOSIS_VIEW,
                Capability.REPORT_VIEW,
                Capability.READINESS_VIEW,
                Capability.MAP_VIEW,
                Capability.DOMAIN_OBSERVE_TRIGGER,
                Capability.RESOLUTION_VERIFY
        )));

        // OWNER: Full organization authority (all capabilities).
        map.put(Role.OWNER, Collections.unmodifiableSet(EnumSet.allOf(Capability.class)));

        MATRIX = Collections.unmodifiableMap(map);
    }

    private RoleCapabilityPolicy() {}

    /**
     * Evaluates if a given role is explicitly authorized for the specified capability.
     *
     * @param role       the user's canonical role
     * @param capability the attempted capability
     * @return true if authorized, false otherwise (default-deny)
     */
    public static boolean hasCapability(Role role, Capability capability) {
        if (role == null || capability == null) {
            return false;
        }
        Set<Capability> authorized = MATRIX.get(role);
        return authorized != null && authorized.contains(capability);
    }

    /**
     * Returns an unmodifiable view of all capabilities granted to the given role.
     *
     * @param role the canonical role
     * @return set of capabilities, or empty set if role is null or unmapped
     */
    public static Set<Capability> getCapabilitiesForRole(Role role) {
        if (role == null) {
            return Collections.emptySet();
        }
        return MATRIX.getOrDefault(role, Collections.emptySet());
    }
}
