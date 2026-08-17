package com.mailsent.ai.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * AiRecommendation — AI-003 Recommendation + confidence (AIR-AI3/7).
 *
 * <p>Represents an actionable resolution path proposed by the AI engine. 
 * Crucially, to maintain strict AI governance (AIR-AI7), this entity enforces 
 * that the AI must declare a confidence score for its recommendation, ensuring
 * users can evaluate the trustworthiness of the automated guidance.</p>
 */
public class AiRecommendation {

    private final String id;
    private final String targetIssueId;
    private final String recommendedAction;
    private final int confidenceScore;
    private final Instant generatedAt;

    private AiRecommendation(String id, String targetIssueId, String recommendedAction, 
                             int confidenceScore, Instant generatedAt) {
        this.id = Objects.requireNonNull(id);
        this.targetIssueId = Objects.requireNonNull(targetIssueId);
        this.recommendedAction = Objects.requireNonNull(recommendedAction);
        this.confidenceScore = confidenceScore;
        this.generatedAt = Objects.requireNonNull(generatedAt);
    }

    /**
     * Proposes a new AI-driven recommendation with an explicit confidence score.
     *
     * @param targetIssueId     the issue this recommendation addresses
     * @param recommendedAction the actionable guidance provided by the AI
     * @param confidenceScore   the AI's certainty score (0-100)
     * @param generatedAt       the deterministic timestamp of generation
     * @return a new AiRecommendation instance
     */
    public static AiRecommendation propose(String targetIssueId, String recommendedAction, 
                                           int confidenceScore, Instant generatedAt) {
        if (recommendedAction.isBlank()) {
            throw new IllegalArgumentException("AI recommendation action cannot be blank.");
        }
        if (confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException("AIR-AI7 violation: Confidence score must be normalized between 0 and 100.");
        }
        return new AiRecommendation(UUID.randomUUID().toString(), targetIssueId, recommendedAction, confidenceScore, generatedAt);
    }

    public String id() { return id; }
    public String targetIssueId() { return targetIssueId; }
    public String recommendedAction() { return recommendedAction; }
    public int confidenceScore() { return confidenceScore; }
    public Instant generatedAt() { return generatedAt; }
}
