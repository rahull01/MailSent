package com.mailsent.monitoring.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.mailsent.portfolio.observation.domain.HealthIndication;

public class HealthChangeEvent {
    private final String id;
    private final String organizationId;
    private final String domainId;
    private final HealthIndication previousIndication;
    private final HealthIndication currentIndication;
    private final Instant detectedAt;

    public HealthChangeEvent(String id, String organizationId, String domainId, HealthIndication previousIndication,
                             HealthIndication currentIndication, Instant detectedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.domainId = Objects.requireNonNull(domainId);
        this.previousIndication = previousIndication; // can be null if no previous signal
        this.currentIndication = Objects.requireNonNull(currentIndication);
        this.detectedAt = Objects.requireNonNull(detectedAt);
    }

    public static HealthChangeEvent detect(String organizationId, String domainId, HealthIndication previous, HealthIndication current, Instant detectedAt) {
        if (previous == current) {
            return null; // No change
        }
        return new HealthChangeEvent(UUID.randomUUID().toString(), organizationId, domainId, previous, current, detectedAt);
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public HealthIndication previousIndication() { return previousIndication; }
    public HealthIndication currentIndication() { return currentIndication; }
    public Instant detectedAt() { return detectedAt; }
}

