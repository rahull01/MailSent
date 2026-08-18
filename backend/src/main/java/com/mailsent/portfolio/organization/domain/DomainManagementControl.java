package com.mailsent.portfolio.organization.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * DomainManagementControl — Controls pause/resume and removal lifecycle states for domains (SET-003).
 */
public class DomainManagementControl {

    public enum MonitoringState {
        ACTIVE,
        PAUSED,
        REMOVED
    }

    private final String id;
    private final String organizationId;
    private final String domainId;
    private MonitoringState state;
    private Instant stateUpdatedAt;
    private String reason;

    private DomainManagementControl(String id, String organizationId, String domainId,
                                    MonitoringState state, Instant stateUpdatedAt, String reason) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.domainId = Objects.requireNonNull(domainId);
        this.state = Objects.requireNonNull(state);
        this.stateUpdatedAt = Objects.requireNonNull(stateUpdatedAt);
        this.reason = reason;
    }

    public static DomainManagementControl init(String organizationId, String domainId, Instant createdAt) {
        return new DomainManagementControl(
                UUID.randomUUID().toString(),
                organizationId,
                domainId,
                MonitoringState.ACTIVE,
                createdAt != null ? createdAt : Instant.now(),
                "Initial registration"
        );
    }

    public void pause(String reason, Instant updatedAt) {
        this.state = MonitoringState.PAUSED;
        this.reason = reason;
        this.stateUpdatedAt = Objects.requireNonNull(updatedAt);
    }

    public void resume(Instant updatedAt) {
        this.state = MonitoringState.ACTIVE;
        this.reason = "Resumed by operator";
        this.stateUpdatedAt = Objects.requireNonNull(updatedAt);
    }

    public void remove(String reason, Instant updatedAt) {
        this.state = MonitoringState.REMOVED;
        this.reason = reason;
        this.stateUpdatedAt = Objects.requireNonNull(updatedAt);
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public MonitoringState state() { return state; }
    public Instant stateUpdatedAt() { return stateUpdatedAt; }
    public String reason() { return reason; }
}
