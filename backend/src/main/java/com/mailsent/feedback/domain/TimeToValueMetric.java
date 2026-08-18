package com.mailsent.feedback.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * TimeToValueMetric — Tracks stage progression and duration for time-to-value analytics (ANAL-003).
 */
public class TimeToValueMetric {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final WorkflowStage stage;
    private final long durationSeconds;
    private final boolean completed;
    private final Instant recordedAt;

    private TimeToValueMetric(String id, String organizationId, String domainId,
                              WorkflowStage stage, long durationSeconds,
                              boolean completed, Instant recordedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.domainId = Objects.requireNonNull(domainId);
        this.stage = Objects.requireNonNull(stage);
        this.durationSeconds = durationSeconds;
        this.completed = completed;
        this.recordedAt = Objects.requireNonNull(recordedAt);
    }

    public static TimeToValueMetric record(String organizationId, String domainId,
                                           WorkflowStage stage, long durationSeconds,
                                           boolean completed, Instant recordedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (domainId.isBlank()) {
            throw new IllegalArgumentException("Domain ID must not be blank.");
        }
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Duration seconds cannot be negative.");
        }
        return new TimeToValueMetric(
                UUID.randomUUID().toString(),
                organizationId,
                domainId,
                stage,
                durationSeconds,
                completed,
                recordedAt != null ? recordedAt : Instant.now()
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public WorkflowStage stage() { return stage; }
    public long durationSeconds() { return durationSeconds; }
    public boolean completed() { return completed; }
    public Instant recordedAt() { return recordedAt; }
}
