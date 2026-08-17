package com.mailsent.ai.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * AiFeedback — AI-004 Transparency & feedback loop (AIR-AI9/12).
 *
 * <p>Represents an explicit user evaluation of an AI-generated output (such as an
 * explanation, prioritization, or recommendation). This structure enforces the 
 * AIR-AI12 requirement for a closed-loop feedback mechanism to continuously monitor,
 * audit, and correct AI performance.</p>
 */
public class AiFeedback {

    public enum FeedbackRating {
        HELPFUL,
        INACCURATE,
        HARMFUL,
        IRRELEVANT
    }

    private final String id;
    private final String aiGeneratedTargetId; // The ID of the Explanation, Recommendation, etc.
    private final String userId;
    private final FeedbackRating rating;
    private final String userComment;
    private final Instant submittedAt;

    private AiFeedback(String id, String aiGeneratedTargetId, String userId, 
                       FeedbackRating rating, String userComment, Instant submittedAt) {
        this.id = Objects.requireNonNull(id);
        this.aiGeneratedTargetId = Objects.requireNonNull(aiGeneratedTargetId);
        this.userId = Objects.requireNonNull(userId);
        this.rating = Objects.requireNonNull(rating);
        this.userComment = userComment; // Optional
        this.submittedAt = Objects.requireNonNull(submittedAt);
    }

    /**
     * Submits structured feedback for an AI output.
     *
     * @param aiGeneratedTargetId the ID of the AI generation being evaluated
     * @param userId              the user providing the feedback
     * @param rating              the categorized rating
     * @param userComment         optional text explaining the rating
     * @param submittedAt         timestamp of submission
     * @return a new AiFeedback instance
     */
    public static AiFeedback submit(String aiGeneratedTargetId, String userId, 
                                    FeedbackRating rating, String userComment, 
                                    Instant submittedAt) {
        if (aiGeneratedTargetId.isBlank()) {
            throw new IllegalArgumentException("Feedback must reference a specific AI output.");
        }
        if (userId.isBlank()) {
            throw new IllegalArgumentException("Feedback must be attributed to a specific user.");
        }
        return new AiFeedback(UUID.randomUUID().toString(), aiGeneratedTargetId, userId, rating, userComment, submittedAt);
    }

    public String id() { return id; }
    public String aiGeneratedTargetId() { return aiGeneratedTargetId; }
    public String userId() { return userId; }
    public FeedbackRating rating() { return rating; }
    public String userComment() { return userComment; }
    public Instant submittedAt() { return submittedAt; }
}
