package com.mailsent.identity.team.application;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessAuditRecord;
import com.mailsent.identity.team.domain.AccessAuditSink;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.domain.Capability;
import com.mailsent.identity.team.domain.RoleCapabilityPolicy;

/**
 * AuthorizationService — Application service for evaluating and enforcing capability authorization (TEAM-002 / TEAM-003).
 *
 * <p>Enforces:
 * 1. Default-Deny: Unauthenticated or unmapped operations are denied.
 * 2. Multi-Tenant Isolation (INV-09): User from Organization A cannot exercise authority in Organization B.
 * 3. Least Authority (NFR-S1): Evaluated against {@link RoleCapabilityPolicy}.
 * 4. Auditing & Logging (NFR-AU / TEAM-003): Decisions are recorded via {@link AccessAuditSink} with MDC diagnostic context.</p>
 */
@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final AccessAuditSink auditSink;
    private final Clock clock;

    public AuthorizationService() {
        this(null, Clock.systemUTC());
    }

    @Autowired
    public AuthorizationService(AccessAuditSink auditSink, Clock clock) {
        this.auditSink = auditSink;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Checks whether a user is authorized to perform a capability within a target organization.
     * Emits an audit record via the audit sink.
     *
     * @param user                 the acting user
     * @param targetOrganizationId the target organization id where the operation is being performed
     * @param capability           the requested capability
     * @return true if authorized, false otherwise (default-deny)
     */
    public boolean isAuthorized(User user, String targetOrganizationId, Capability capability) {
        Instant now = Instant.now(clock);
        String userId = user != null ? user.id() : null;
        String userOrg = user != null ? user.organizationId() : null;

        if (user == null) {
            recordAudit(AccessAuditRecord.denied(now, null, null, targetOrganizationId, capability, "Unauthenticated / null user"));
            return false;
        }

        if (targetOrganizationId == null || targetOrganizationId.isBlank()) {
            recordAudit(AccessAuditRecord.denied(now, userId, userOrg, targetOrganizationId, capability, "Missing target organization ID"));
            return false;
        }

        if (capability == null) {
            recordAudit(AccessAuditRecord.denied(now, userId, userOrg, targetOrganizationId, null, "Null capability requested"));
            return false;
        }

        // Multi-Tenant Isolation Invariant (INV-09): User's home organization must match target organization
        if (user.organizationId() == null || !user.organizationId().equals(targetOrganizationId)) {
            recordAudit(AccessAuditRecord.denied(now, userId, userOrg, targetOrganizationId, capability,
                    String.format("Multi-tenant violation: user home org '%s' does not match target org '%s'", userOrg, targetOrganizationId)));
            return false;
        }

        boolean authorized = RoleCapabilityPolicy.hasCapability(user.role(), capability);
        if (authorized) {
            recordAudit(AccessAuditRecord.granted(now, userId, userOrg, targetOrganizationId, capability));
        } else {
            String roleName = user.role() != null ? user.role().name() : "none";
            recordAudit(AccessAuditRecord.denied(now, userId, userOrg, targetOrganizationId, capability,
                    String.format("Role %s not granted capability %s", roleName, capability)));
        }

        return authorized;
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
     * Throws {@link AccessDeniedException} if authorization fails.
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

            throw new AccessDeniedException(userId, targetOrganizationId, capability,
                    String.format("User %s with role %s is not authorized for %s on organization %s",
                            userId, roleName, capability, targetOrganizationId));
        }
    }

    private void recordAudit(AccessAuditRecord record) {
        if (auditSink != null) {
            try {
                auditSink.record(record);
            } catch (Exception e) {
                // Availability-over-completeness: authorization still applies, but the
                // audit loss must be loud (N3 no-silent-loss) and carry MDC context.
                log.error("Access audit write failed for actor '{}' homeOrg '{}' targetOrg '{}' capability '{}' decision '{}'",
                        record.userId(), record.userOrganizationId(), record.targetOrganizationId(),
                        record.capability(), record.decision(), e);
            }
        }
    }
}
