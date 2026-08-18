package com.mailsent.feedback.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * AnalyticsEvent — Telemetry record representing a key user/system action (ANAL-001).
 */
public class AnalyticsEvent {

    private final String id;
    private final String organizationId;
    private final AnalyticsEventType eventType;
    private final String entityId;
    private final String userId;
    private final Map<String, String> metadata;
    private final Instant occurredAt;

    private AnalyticsEvent(String id, String organizationId, AnalyticsEventType eventType,
                           String entityId, String userId, Map<String, String> metadata,
                           Instant occurredAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.eventType = Objects.requireNonNull(eventType);
        this.entityId = Objects.requireNonNull(entityId);
        this.userId = Objects.requireNonNull(userId);
        this.metadata = Collections.unmodifiableMap(Objects.requireNonNull(metadata));
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public static AnalyticsEvent capture(String organizationId, AnalyticsEventType eventType,
                                         String entityId, String userId,
                                         Map<String, String> metadata, Instant occurredAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (entityId.isBlank()) {
            throw new IllegalArgumentException("Entity ID must not be blank.");
        }
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank.");
        }
        return new AnalyticsEvent(
                UUID.randomUUID().toString(),
                organizationId,
                eventType,
                entityId,
                userId,
                metadata != null ? metadata : Collections.emptyMap(),
                occurredAt != null ? occurredAt : Instant.now()
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public AnalyticsEventType eventType() { return eventType; }
    public String entityId() { return entityId; }
    public String userId() { return userId; }
    public Map<String, String> metadata() { return metadata; }
    public Instant occurredAt() { return occurredAt; }
}
