package com.mailsent.common.security;

import java.util.Map;
import java.util.Set;

/**
 * PrivilegeEnforcer — Enforces least-privilege role boundaries across bounded contexts (SEC-002 / NFR-S1).
 */
public final class PrivilegeEnforcer {

    public enum Role {
        OWNER,
        ADMIN,
        OPERATOR,
        ANALYST,
        VIEWER
    }

    public enum Permission {
        VIEW_HEALTH,
        TRIGGER_SCAN,
        GRANT_CONSENT_FIX,
        MANAGE_SEATS,
        MODIFY_BILLING,
        DELETE_DOMAIN
    }

    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            Role.OWNER, Set.of(Permission.values()),
            Role.ADMIN, Set.of(
                    Permission.VIEW_HEALTH,
                    Permission.TRIGGER_SCAN,
                    Permission.GRANT_CONSENT_FIX,
                    Permission.MANAGE_SEATS,
                    Permission.DELETE_DOMAIN
            ),
            Role.OPERATOR, Set.of(
                    Permission.VIEW_HEALTH,
                    Permission.TRIGGER_SCAN,
                    Permission.GRANT_CONSENT_FIX
            ),
            Role.ANALYST, Set.of(
                    Permission.VIEW_HEALTH,
                    Permission.TRIGGER_SCAN
            ),
            Role.VIEWER, Set.of(
                    Permission.VIEW_HEALTH
            )
    );

    private PrivilegeEnforcer() {}

    public static boolean hasPermission(Role role, Permission permission) {
        if (role == null || permission == null) return false;
        Set<Permission> perms = ROLE_PERMISSIONS.getOrDefault(role, Set.of());
        return perms.contains(permission);
    }

    public static void checkPermission(Role role, Permission permission) {
        if (!hasPermission(role, permission)) {
            throw new SecurityException("Access Denied (NFR-S1): Role " + role + " lacks permission " + permission);
        }
    }
}
