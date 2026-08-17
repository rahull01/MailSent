package com.mailsent.ai.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * AiConsentGatedAction — AI-005 Consent-gated action lane (AIR-AI4).
 *
 * <p>Represents an automated action proposed by the AI that strictly requires explicit
 * user consent before execution. This structure enforces the AIR-AI4 safety requirement,
 * guaranteeing that the AI cannot autonomously mutate critical system state (e.g., DNS 
 * records, sending configurations) without human-in-the-loop authorization.</p>
 */
public class AiConsentGatedAction {

    public enum ActionStatus {
        PROPOSED,
        CONSENT_GRANTED,
        CONSENT_DENIED,
        EXECUTED,
        FAILED
    }

    private final String id;
    private final String targetRecommendationId; // Link back to the AiRecommendation
    private final String proposedActionPayload;
    private ActionStatus status;
    private Instant statusUpdatedAt;

    private AiConsentGatedAction(String id, String targetRecommendationId, 
                                 String proposedActionPayload, ActionStatus status, 
                                 Instant statusUpdatedAt) {
        this.id = Objects.requireNonNull(id);
        this.targetRecommendationId = Objects.requireNonNull(targetRecommendationId);
        this.proposedActionPayload = Objects.requireNonNull(proposedActionPayload);
        this.status = Objects.requireNonNull(status);
        this.statusUpdatedAt = Objects.requireNonNull(statusUpdatedAt);
    }

    /**
     * Proposes a new action requiring human consent.
     *
     * @param targetRecommendationId the recommendation driving this action
     * @param proposedActionPayload  the concrete system action to execute
     * @param proposedAt             the timestamp of proposal
     * @return a new AiConsentGatedAction instance
     */
    public static AiConsentGatedAction propose(String targetRecommendationId, 
                                               String proposedActionPayload, 
                                               Instant proposedAt) {
        if (proposedActionPayload.isBlank()) {
            throw new IllegalArgumentException("Proposed action payload cannot be empty.");
        }
        return new AiConsentGatedAction(UUID.randomUUID().toString(), targetRecommendationId, 
                                        proposedActionPayload, ActionStatus.PROPOSED, proposedAt);
    }

    /**
     * Grants human consent to execute the proposed action.
     *
     * @param grantedAt the timestamp consent was granted
     */
    public void grantConsent(Instant grantedAt) {
        if (this.status != ActionStatus.PROPOSED) {
            throw new IllegalStateException("Consent can only be granted for proposed actions.");
        }
        this.status = ActionStatus.CONSENT_GRANTED;
        this.statusUpdatedAt = Objects.requireNonNull(grantedAt);
    }

    public String id() { return id; }
    public String targetRecommendationId() { return targetRecommendationId; }
    public String proposedActionPayload() { return proposedActionPayload; }
    public ActionStatus status() { return status; }
    public Instant statusUpdatedAt() { return statusUpdatedAt; }
}
