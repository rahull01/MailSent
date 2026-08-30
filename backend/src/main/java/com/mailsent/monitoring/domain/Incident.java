package com.mailsent.monitoring.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Incident {
    private final String id;
    private final String organizationId;
    private final String domainId;
    private final Instant openedAt;
    private Instant resolvedAt;

    public Incident(String id, String organizationId, String domainId, Instant openedAt, Instant resolvedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.domainId = Objects.requireNonNull(domainId);
        this.openedAt = Objects.requireNonNull(openedAt);
        this.resolvedAt = resolvedAt;
    }

    public static Incident open(String organizationId, String domainId, Instant openedAt) {
        return new Incident(UUID.randomUUID().toString(), organizationId, domainId, openedAt, null);
    }

    public void resolve(Instant resolvedAt) {
        if (this.resolvedAt != null) {
            throw new IllegalStateException("Incident is already resolved");
        }
        this.resolvedAt = Objects.requireNonNull(resolvedAt);
    }

    public boolean isResolved() {
        return resolvedAt != null;
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public Instant openedAt() { return openedAt; }
    public Instant resolvedAt() { return resolvedAt; }
}

