package com.mailsent.feedback.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * InAppUsefulnessFeedback — Captures user usefulness feedback on specific outputs (ANAL-004 / FUN-F1).
 */
public class InAppUsefulnessFeedback {

    private final String id;
    private final String organizationId;
    private final String userId;
    private final FeedbackTargetType targetType;
    private final String targetId;
    private final boolean useful;
    private final String comment;
    private final Instant submittedAt;

    private InAppUsefulnessFeedback(String id, String organizationId, String userId,
                                    FeedbackTargetType targetType, String targetId,
                                    boolean useful, String comment, Instant submittedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.userId = Objects.requireNonNull(userId);
        this.targetType = Objects.requireNonNull(targetType);
        this.targetId = Objects.requireNonNull(targetId);
        this.useful = useful;
        this.comment = comment;
        this.submittedAt = Objects.requireNonNull(submittedAt);
    }

    public static InAppUsefulnessFeedback record(String organizationId, String userId,
                                                 FeedbackTargetType targetType, String targetId,
                                                 boolean useful, String comment, Instant submittedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank.");
        }
        if (targetId.isBlank()) {
            throw new IllegalArgumentException("Target ID must not be blank.");
        }
        return new InAppUsefulnessFeedback(
                UUID.randomUUID().toString(),
                organizationId,
                userId,
                targetType,
                targetId,
                useful,
                comment,
                submittedAt != null ? submittedAt : Instant.now()
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String userId() { return userId; }
    public FeedbackTargetType targetType() { return targetType; }
    public String targetId() { return targetId; }
    public boolean isUseful() { return useful; }
    public String comment() { return comment; }
    public Instant submittedAt() { return submittedAt; }
}
