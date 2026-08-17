package com.mailsent.evidence.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * AuditTrailRecord — EVID-002 Audit trail record (INV-11).
 *
 * <p>Represents an immutable record of system lineage. INV-11 mandates that every
 * conclusion (such as an Assessment, Health Signal, or Explanation) traces explicitly
 * to the actual evidence source snapshot. This entity acts as the universal audit trail
 * capturing the link between the underlying source data and the resulting system outcome.</p>
 */
public class AuditTrailRecord {

    private final String id;
    private final String actionName;
    private final String sourceEvidenceId;
    private final String resultingConclusionId;
    private final Instant recordedAt;

    private AuditTrailRecord(String id, String actionName, String sourceEvidenceId, 
                             String resultingConclusionId, Instant recordedAt) {
        this.id = Objects.requireNonNull(id);
        this.actionName = Objects.requireNonNull(actionName);
        this.sourceEvidenceId = Objects.requireNonNull(sourceEvidenceId);
        this.resultingConclusionId = Objects.requireNonNull(resultingConclusionId);
        this.recordedAt = Objects.requireNonNull(recordedAt);
    }

    /**
     * Records a new audit trail for a system conclusion.
     *
     * @param actionName            the operation that drew the conclusion (e.g., "HEALTH_SIGNAL_GENERATION")
     * @param sourceEvidenceId      the canonical identity of the raw evidence snapshot (Observation Evidence, DMARC, etc.)
     * @param resultingConclusionId the canonical identity of the generated outcome (Health Signal, Explanation, etc.)
     * @param recordedAt            the deterministic timestamp of the audit event
     * @return a new AuditTrailRecord instance
     */
    public static AuditTrailRecord record(String actionName, String sourceEvidenceId, 
                                          String resultingConclusionId, Instant recordedAt) {
        if (actionName.isBlank() || sourceEvidenceId.isBlank() || resultingConclusionId.isBlank()) {
            throw new IllegalArgumentException("INV-11 violation: Audit trail must strictly identify the action, source, and conclusion.");
        }
        return new AuditTrailRecord(UUID.randomUUID().toString(), actionName, sourceEvidenceId, resultingConclusionId, recordedAt);
    }

    public String id() { return id; }
    public String actionName() { return actionName; }
    public String sourceEvidenceId() { return sourceEvidenceId; }
    public String resultingConclusionId() { return resultingConclusionId; }
    public Instant recordedAt() { return recordedAt; }
}
