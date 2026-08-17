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

    private Report(String id, String targetId, TargetScope scope, String contentBody, Instant generatedAt) {
        this.id = Objects.requireNonNull(id);
        this.targetId = Objects.requireNonNull(targetId);
        this.scope = Objects.requireNonNull(scope);
        this.contentBody = Objects.requireNonNull(contentBody);
        this.generatedAt = Objects.requireNonNull(generatedAt);
    }

    /**
     * Generates a new Report.
     *
     * @param targetId    the identity of the domain or portfolio organization
     * @param scope       whether this covers a single domain or full portfolio
     * @param contentBody the compiled report body
     * @param generatedAt the deterministic generation timestamp
     * @return a new Report instance
     */
    public static Report generate(String targetId, TargetScope scope, String contentBody, Instant generatedAt) {
        if (contentBody.isBlank()) {
            throw new IllegalArgumentException("A report must contain evidence content.");
        }
        return new Report(UUID.randomUUID().toString(), targetId, scope, contentBody, generatedAt);
    }

    public String id() { return id; }
    public String targetId() { return targetId; }
    public TargetScope scope() { return scope; }
    public String contentBody() { return contentBody; }
    public Instant generatedAt() { return generatedAt; }
}
