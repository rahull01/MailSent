package com.mailsent.reporting.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Report — REP-002 Report generator (FUN-R2).
 *
 * <p>Represents a point-in-time report demonstrating the health state of a domain
 * or an entire portfolio. FUN-R2 mandates this so a user can provide evidence of
 * health to a client, auditor, or board.</p>
 */
public class Report {

    public enum TargetScope {
        /** The report covers a single domain. */
        DOMAIN,
        /** The report covers an entire organization's portfolio. */
        PORTFOLIO
    }

    private final String id;
    private final String targetId;
    private final TargetScope scope;
    private final String contentBody; // Simplified placeholder for the actual report payload
    private final Instant generatedAt;
    private final WhiteLabelConfig whiteLabelConfig;

    private Report(String id, String targetId, TargetScope scope, String contentBody,
                   Instant generatedAt, WhiteLabelConfig whiteLabelConfig) {
        this.id = Objects.requireNonNull(id);
        this.targetId = Objects.requireNonNull(targetId);
        this.scope = Objects.requireNonNull(scope);
        this.contentBody = Objects.requireNonNull(contentBody);
        this.generatedAt = Objects.requireNonNull(generatedAt);
        this.whiteLabelConfig = whiteLabelConfig != null ? whiteLabelConfig : WhiteLabelConfig.defaultBranding();
    }

    /**
     * Generates a new Report with optional white-label branding metadata (REP-004).
     *
     * <p>Enforces INV-09 multi-tenant isolation: If the whiteLabelConfig is explicitly bound
     * to an agency organization ID, the targetId of a PORTFOLIO report or the owning scope must
     * match that agency organization.</p>
     *
     * @param targetId         the identity of the domain or portfolio organization
     * @param scope            whether this covers a single domain or full portfolio
     * @param contentBody      the compiled report body
     * @param generatedAt      the deterministic generation timestamp
     * @param whiteLabelConfig the optional white-label branding configuration
     * @return a new Report instance
     * @throws IllegalArgumentException if contentBody is blank or tenant isolation is violated (INV-09)
     */
    public static Report generate(String targetId, TargetScope scope, String contentBody,
                                  Instant generatedAt, WhiteLabelConfig whiteLabelConfig) {
        if (contentBody.isBlank()) {
            throw new IllegalArgumentException("A report must contain evidence content.");
        }
        if (whiteLabelConfig != null && whiteLabelConfig.agencyOrganizationId() != null) {
            String brandingOrgId = whiteLabelConfig.agencyOrganizationId();
            if (scope == TargetScope.PORTFOLIO && !targetId.equals(brandingOrgId)) {
                throw new IllegalArgumentException(
                    "WhiteLabelConfig owned by agency '" + brandingOrgId +
                    "' cannot be attached to a report targeting organization '" + targetId + "' (INV-09).");
            }
        }
        return new Report(UUID.randomUUID().toString(), targetId, scope, contentBody, generatedAt, whiteLabelConfig);
    }

    /**
     * Generates a new standard unbranded Report (backwards-compatible overload).
     *
     * @param targetId    the identity of the domain or portfolio organization
     * @param scope       whether this covers a single domain or full portfolio
     * @param contentBody the compiled report body
     * @param generatedAt the deterministic generation timestamp
     * @return a new Report instance
     */
    public static Report generate(String targetId, TargetScope scope, String contentBody, Instant generatedAt) {
        return generate(targetId, scope, contentBody, generatedAt, null);
    }

    public String id() { return id; }
    public String targetId() { return targetId; }
    public TargetScope scope() { return scope; }
    public String contentBody() { return contentBody; }
    public Instant generatedAt() { return generatedAt; }
    public WhiteLabelConfig whiteLabelConfig() { return whiteLabelConfig; }
}
