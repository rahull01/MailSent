package com.mailsent.identity.team.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.domain.Capability;
import com.mailsent.identity.team.domain.RoleCapabilityPolicy;

/**
 * AuthorizationService — Application service for evaluating and enforcing capability authorization (TEAM-002).
 *
 * <p>Enforces:
 * 1. Default-Deny: Unauthenticated or unmapped operations are denied.
 * 2. Multi-Tenant Isolation (INV-09): User from Organization A cannot exercise authority in Organization B.
 * 3. Least Authority (NFR-S1): Evaluated against {@link RoleCapabilityPolicy}.
 * 4. Auditing & Logging (N3): Denials are logged with MDC diagnostic context.</p>
 */
@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    /**
     * Checks whether a user is authorized to perform a capability within a target organization.
     *
     * @param user               the acting user
     * @param targetOrganizationId the target organization id where the operation is being performed
     * @param capability         the requested capability
     * @return true if authorized, false otherwise (default-deny)
     */
    public boolean isAuthorized(User user, String targetOrganizationId, Capability capability) {
        if (user == null || targetOrganizationId == null || targetOrganizationId.isBlank() || capability == null) {
            return false;
        }

        // Multi-Tenant Isolation Invariant (INV-09): User's home organization must match target organization
        if (user.organizationId() == null || !user.organizationId().equals(targetOrganizationId)) {
            return false;
        }

        return RoleCapabilityPolicy.hasCapability(user.role(), capability);
    }

    /**
     * Checks whether a raw role is authorized for a capability within an organization context.
     *
     * @param role       the role
     * @param capability the requested capability
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorized(Role role, Capability capability) {
        if (role == null || capability == null) {
            return false;
        }
        return RoleCapabilityPolicy.hasCapability(role, capability);
    }

    /**
     * Asserts that a user is authorized for a capability in the target organization.
     * Throws {@link AccessDeniedException} and logs MDC audit context if authorization fails.
     *
     * @param user                 the acting user
     * @param targetOrganizationId the target organization
     * @param capability           the requested capability
     * @throws AccessDeniedException if not authorized or tenant mismatch
     */
    public void checkAuthorization(User user, String targetOrganizationId, Capability capability) {
        if (!isAuthorized(user, targetOrganizationId, capability)) {
            String userId = user != null ? user.id() : "anonymous";
            String userOrg = user != null ? user.organizationId() : "none";
            String roleName = user != null && user.role() != null ? user.role().name() : "none";

            log.warn("Access Denied: userId={}, userOrg={}, targetOrg={}, role={}, capability={}",
                    userId, userOrg, targetOrganizationId, roleName, capability);

            throw new AccessDeniedException(userId, targetOrganizationId, capability,
                    String.format("User %s with role %s is not authorized for %s on organization %s",
                            userId, roleName, capability, targetOrganizationId));
        }
    }
}
