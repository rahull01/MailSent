package com.mailsent.explanation.domain;

import java.util.List;
import java.util.Objects;

import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * Grounding - the evidence trail for a business explanation (EXPL-004).
 *
 * <p>Ensures that every explanation traces strictly to the authoritative
 * observed facts (INV-04, AIR-AI5/6). An explanation without grounding
 * is invalid.</p>
 */
public final class Grounding {
    private final String sourceSignalId;
    private final String observationEvidenceId;
    private final List<String> dmarcAggregateEvidenceIds;
    private final List<String> reputationObservationIds;

    private Grounding(String sourceSignalId, String observationEvidenceId, 
                      List<String> dmarcAggregateEvidenceIds, 
                      List<String> reputationObservationIds) {
        this.sourceSignalId = sourceSignalId;
        this.observationEvidenceId = observationEvidenceId;
        this.dmarcAggregateEvidenceIds = List.copyOf(dmarcAggregateEvidenceIds);
        this.reputationObservationIds = List.copyOf(reputationObservationIds);
    }

    public static Grounding from(HealthSignal signal) {
        Objects.requireNonNull(signal, "Source signal is required for grounding.");
        return new Grounding(
            signal.id(),
            signal.observationEvidenceId(),
            signal.dmarcAggregateEvidenceIds(),
            signal.reputationObservationIds()
        );
    }

    public String sourceSignalId() { return sourceSignalId; }
    public String observationEvidenceId() { return observationEvidenceId; }
    public List<String> dmarcAggregateEvidenceIds() { return dmarcAggregateEvidenceIds; }
    public List<String> reputationObservationIds() { return reputationObservationIds; }
}
