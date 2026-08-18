package com.mailsent.delivery.ingest;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * DmarcIngestTrigger — In-process trigger for DIAG-001 upon new RUA evidence (INT-006).
 */
public class DmarcIngestTrigger {

    @FunctionalInterface
    public interface DiagnosisTriggerCallback {
        void onEvidenceAvailable(String domainId, String organizationId, String evidenceId);
    }

    private final Set<String> processedReportKeys = new HashSet<>();
    private final DiagnosisTriggerCallback callback;

    public DmarcIngestTrigger(DiagnosisTriggerCallback callback) {
        this.callback = Objects.requireNonNull(callback);
    }

    /**
     * Triggers DIAG-001 in-process when valid DMARC Aggregate Evidence exists.
     * Enforces idempotency: duplicate report_id scoped to the same domain does not re-trigger.
     */
    public synchronized boolean evaluateAndTrigger(DmarcAggregateEvidence evidence) {
        Objects.requireNonNull(evidence);

        String deduplicationKey = evidence.domainId() + ":" + evidence.reportId();
        if (processedReportKeys.contains(deduplicationKey)) {
            // Already processed; do not re-trigger analysis
            return false;
        }

        processedReportKeys.add(deduplicationKey);
        callback.onEvidenceAvailable(evidence.domainId(), evidence.organizationId(), evidence.id());
        return true;
    }
}
