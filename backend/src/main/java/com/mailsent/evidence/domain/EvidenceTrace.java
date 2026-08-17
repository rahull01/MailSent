package com.mailsent.evidence.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * EvidenceTrace — EVID-001 Report grounding (FUN-R4).
 *
 * <p>Represents a strict logical trace linking a specific assertion or conclusion
 * in a generated report to the exact raw observation that grounds it. FUN-R4
 * rigidly mandates that every piece of evidence trace back to a real observation,
 * structurally prohibiting ungrounded or fabricated health conclusions.</p>
 */
public class EvidenceTrace {

    private final String id;
    private final String reportId;
    private final String observationId;
    private final String findingAssertion;
    private final Instant tracedAt;

    private EvidenceTrace(String id, String reportId, String observationId, 
                          String findingAssertion, Instant tracedAt) {
        this.id = Objects.requireNonNull(id);
        this.reportId = Objects.requireNonNull(reportId);
        this.observationId = Objects.requireNonNull(observationId);
        this.findingAssertion = Objects.requireNonNull(findingAssertion);
        this.tracedAt = Objects.requireNonNull(tracedAt);
    }

    /**
     * Creates a traceable link between a report assertion and a source observation.
     *
     * @param reportId         the identity of the generated report
     * @param observationId    the identity of the canonical raw observation
     * @param findingAssertion the specific statement in the report being grounded
     * @param tracedAt         the deterministic timestamp of this trace linkage
     * @return a new EvidenceTrace instance
     */
    public static EvidenceTrace link(String reportId, String observationId, 
                                     String findingAssertion, Instant tracedAt) {
        if (findingAssertion == null || findingAssertion.isBlank()) {
            throw new IllegalArgumentException("FUN-R4 violation: an evidence trace must assert a specific report finding.");
        }
        return new EvidenceTrace(UUID.randomUUID().toString(), reportId, observationId, findingAssertion, tracedAt);
    }

    public String id() { return id; }
    public String reportId() { return reportId; }
    public String observationId() { return observationId; }
    public String findingAssertion() { return findingAssertion; }
    public Instant tracedAt() { return tracedAt; }
}
