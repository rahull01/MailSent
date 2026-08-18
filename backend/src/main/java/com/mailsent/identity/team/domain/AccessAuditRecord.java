package com.mailsent.identity.team.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * AccessAuditRecord — Immutable domain Value Object representing an evaluated access/authorization event (TEAM-003).
 *
 * <p>Captures the actor identity, home organization vs target organization boundary (INV-09),
 * the requested capability, the access decision (GRANTED or DENIED), and the reason for denial.</p>
 */
public record AccessAuditRecord(
        Instant occurredAt,
        String userId,
        String userOrganizationId,
        String targetOrganizationId,
        Capability capability,
        AccessDecision decision,
        String reason
) {

    public AccessAuditRecord {
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        userId = (userId != null && !userId.isBlank()) ? userId : "anonymous";
        userOrganizationId = (userOrganizationId != null && !userOrganizationId.isBlank()) ? userOrganizationId : "none";
        targetOrganizationId = (targetOrganizationId != null && !targetOrganizationId.isBlank()) ? targetOrganizationId : "none";
        reason = (reason != null) ? reason : "";
    }

    /**
     * Factory for an authorized/granted access audit event.
     */
    public static AccessAuditRecord granted(Instant now, String userId, String userOrgId, String targetOrgId, Capability capability) {
        return new AccessAuditRecord(now, userId, userOrgId, targetOrgId, capability, AccessDecision.GRANTED, "Authorized");
    }

    /**
     * Factory for a denied access audit event.
     */
    public static AccessAuditRecord denied(Instant now, String userId, String userOrgId, String targetOrgId, Capability capability, String reason) {
        return new AccessAuditRecord(now, userId, userOrgId, targetOrgId, capability, AccessDecision.DENIED, reason);
    }
}
