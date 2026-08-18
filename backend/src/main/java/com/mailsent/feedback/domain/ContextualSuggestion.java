package com.mailsent.feedback.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * ContextualSuggestion — User-submitted suggestions and problem reports for continuous improvement (ANAL-005 / FUN-F2).
 */
public class ContextualSuggestion {

    private final String id;
    private final String organizationId;
    private final String userId;
    private final String contextType;
    private final String contextId;
    private final String suggestedChange;
    private final String reasoning;
    private SuggestionStatus status;
    private final Instant submittedAt;

    private ContextualSuggestion(String id, String organizationId, String userId,
                                 String contextType, String contextId,
                                 String suggestedChange, String reasoning,
                                 SuggestionStatus status, Instant submittedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.userId = Objects.requireNonNull(userId);
        this.contextType = Objects.requireNonNull(contextType);
        this.contextId = Objects.requireNonNull(contextId);
        this.suggestedChange = Objects.requireNonNull(suggestedChange);
        this.reasoning = Objects.requireNonNull(reasoning);
        this.status = Objects.requireNonNull(status);
        this.submittedAt = Objects.requireNonNull(submittedAt);
    }

    public static ContextualSuggestion create(String organizationId, String userId,
                                              String contextType, String contextId,
                                              String suggestedChange, String reasoning,
                                              Instant submittedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank.");
        }
        if (suggestedChange.isBlank()) {
            throw new IllegalArgumentException("Suggested change must not be blank.");
        }
        if (reasoning.isBlank()) {
            throw new IllegalArgumentException("Reasoning must not be blank.");
        }
        return new ContextualSuggestion(
                UUID.randomUUID().toString(),
                organizationId,
                userId,
                contextType,
                contextId,
                suggestedChange,
                reasoning,
                SuggestionStatus.PENDING_REVIEW,
                submittedAt != null ? submittedAt : Instant.now()
        );
    }

    public void updateStatus(SuggestionStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus);
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String userId() { return userId; }
    public String contextType() { return contextType; }
    public String contextId() { return contextId; }
    public String suggestedChange() { return suggestedChange; }
    public String reasoning() { return reasoning; }
    public SuggestionStatus status() { return status; }
    public Instant submittedAt() { return submittedAt; }
}
