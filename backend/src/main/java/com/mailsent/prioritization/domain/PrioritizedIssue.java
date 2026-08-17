package com.mailsent.prioritization.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * PrioritizedIssue — AI-002 Prioritization engine (FUN-P2).
 *
 * <p>Represents an issue that has been evaluated and ranked by the AI engine.
 * Satisfies FUN-P2 by assigning a specific, sortable priority weight to findings
 * so the user knows what must be addressed first based on business risk.</p>
 */
public class PrioritizedIssue {

    private final String id;
    private final String sourceFindingId;
    private final int priorityScore; // Higher value = higher business risk
    private final String aiJustification;
    private final Instant prioritizedAt;

    private PrioritizedIssue(String id, String sourceFindingId, int priorityScore, 
                             String aiJustification, Instant prioritizedAt) {
        this.id = Objects.requireNonNull(id);
        this.sourceFindingId = Objects.requireNonNull(sourceFindingId);
        this.priorityScore = priorityScore;
        this.aiJustification = Objects.requireNonNull(aiJustification);
        this.prioritizedAt = Objects.requireNonNull(prioritizedAt);
    }

    /**
     * Ranks a finding with a specific business priority score.
     *
     * @param sourceFindingId the finding being evaluated
     * @param priorityScore   the calculated risk score (higher = more critical)
     * @param aiJustification the reasoning for why this score was assigned
     * @param prioritizedAt   the deterministic timestamp of evaluation
     * @return a new PrioritizedIssue instance
     */
    public static PrioritizedIssue rank(String sourceFindingId, int priorityScore, 
                                        String aiJustification, Instant prioritizedAt) {
        if (priorityScore < 0 || priorityScore > 100) {
            throw new IllegalArgumentException("Priority score must be a normalized value between 0 and 100.");
        }
        if (aiJustification.isBlank()) {
            throw new IllegalArgumentException("AI prioritization must include a justification for the score.");
        }
        return new PrioritizedIssue(UUID.randomUUID().toString(), sourceFindingId, priorityScore, aiJustification, prioritizedAt);
    }

    public String id() { return id; }
    public String sourceFindingId() { return sourceFindingId; }
    public int priorityScore() { return priorityScore; }
    public String aiJustification() { return aiJustification; }
    public Instant prioritizedAt() { return prioritizedAt; }
}
