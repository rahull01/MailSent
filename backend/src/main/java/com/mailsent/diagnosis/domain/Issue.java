package com.mailsent.diagnosis.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Issue — Aggregate Root representing a diagnosed delivery problem on a domain.
 * 
 * <p>Preserves immutable origin lineage (originHealthSignalId, originDmarcAggregateEvidenceIds,
 * discoveredAt) while tracking ongoing state updates (latestHealthSignalId,
 * latestDmarcAggregateEvidenceIds, updatedAt).</p>
 */
public class Issue {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final RootCause rootCause;
    private final Sender sender; // nullable
    private final String originHealthSignalId;
    private final List<String> originDmarcAggregateEvidenceIds;
    private final Instant discoveredAt;

    private IssueStatus status;
    private String latestHealthSignalId;
    private List<String> latestDmarcAggregateEvidenceIds;
    private Instant resolvedAt;
    private Instant closedAt;
    private Instant updatedAt;

    private Issue(String id, String organizationId, String domainId, String domainName,
                  RootCause rootCause, Sender sender,
                  String originHealthSignalId, List<String> originDmarcAggregateEvidenceIds,
                  String latestHealthSignalId, List<String> latestDmarcAggregateEvidenceIds,
                  IssueStatus status, Instant discoveredAt, Instant resolvedAt, Instant closedAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId cannot be null");
        this.domainId = Objects.requireNonNull(domainId, "domainId cannot be null");
        this.domainName = Objects.requireNonNull(domainName, "domainName cannot be null");
        this.rootCause = Objects.requireNonNull(rootCause, "rootCause cannot be null");
        this.sender = sender;
        this.originHealthSignalId = Objects.requireNonNull(originHealthSignalId, "originHealthSignalId cannot be null");
        this.originDmarcAggregateEvidenceIds = List.copyOf(Objects.requireNonNull(originDmarcAggregateEvidenceIds, "originDmarcAggregateEvidenceIds cannot be null"));
        this.latestHealthSignalId = Objects.requireNonNull(latestHealthSignalId, "latestHealthSignalId cannot be null");
        this.latestDmarcAggregateEvidenceIds = List.copyOf(Objects.requireNonNull(latestDmarcAggregateEvidenceIds, "latestDmarcAggregateEvidenceIds cannot be null"));
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.discoveredAt = Objects.requireNonNull(discoveredAt, "discoveredAt cannot be null");
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public static Issue discover(String organizationId, String domainId, String domainName,
                                 RootCause rootCause, Sender sender,
                                 String healthSignalId, List<String> evidenceIds, Instant discoveredAt) {
        if (evidenceIds == null || evidenceIds.isEmpty()) {
            throw new IllegalArgumentException("evidenceIds cannot be null or empty");
        }
        Instant now = discoveredAt != null ? discoveredAt : Instant.now();
        return new Issue(
                UUID.randomUUID().toString(),
                organizationId,
                domainId,
                domainName,
                rootCause,
                sender,
                healthSignalId,
                evidenceIds,
                healthSignalId,
                evidenceIds,
                IssueStatus.DISCOVERED,
                now,
                null,
                null,
                now
        );
    }

    public static Issue restore(String id, String organizationId, String domainId, String domainName,
                                RootCause rootCause, Sender sender,
                                String originHealthSignalId, List<String> originDmarcAggregateEvidenceIds,
                                String latestHealthSignalId, List<String> latestDmarcAggregateEvidenceIds,
                                IssueStatus status, Instant discoveredAt, Instant resolvedAt, Instant closedAt, Instant updatedAt) {
        return new Issue(
                id, organizationId, domainId, domainName, rootCause, sender,
                originHealthSignalId, originDmarcAggregateEvidenceIds,
                latestHealthSignalId, latestDmarcAggregateEvidenceIds,
                status, discoveredAt, resolvedAt, closedAt, updatedAt
        );
    }

    public void diagnose(Instant now) {
        if (this.status != IssueStatus.DISCOVERED) {
            throw new IllegalStateException("Can only diagnose an issue currently in DISCOVERED status. Current status: " + this.status);
        }
        this.status = IssueStatus.DIAGNOSED;
        this.updatedAt = now != null ? now : Instant.now();
    }

    public void resolve(Instant now) {
        if (this.status == IssueStatus.CLOSED) {
            throw new IllegalStateException("Cannot resolve a CLOSED issue. Current status: " + this.status);
        }
        this.status = IssueStatus.RESOLVED;
        this.resolvedAt = now != null ? now : Instant.now();
        this.updatedAt = this.resolvedAt;
    }

    public void close(Instant now) {
        this.status = IssueStatus.CLOSED;
        this.closedAt = now != null ? now : Instant.now();
        this.updatedAt = this.closedAt;
    }

    public void reopen(Instant now) {
        if (this.status != IssueStatus.RESOLVED && this.status != IssueStatus.CLOSED) {
            throw new IllegalStateException("Can only reopen a RESOLVED or CLOSED issue. Current status: " + this.status);
        }
        this.status = IssueStatus.DISCOVERED;
        this.resolvedAt = null;
        this.closedAt = null;
        this.updatedAt = now != null ? now : Instant.now();
    }

    public void recordSubsequentObservation(String newHealthSignalId, List<String> newEvidenceIds, Instant now) {
        Objects.requireNonNull(newHealthSignalId, "newHealthSignalId cannot be null");
        Objects.requireNonNull(newEvidenceIds, "newEvidenceIds cannot be null");
        this.latestHealthSignalId = newHealthSignalId;
        this.latestDmarcAggregateEvidenceIds = List.copyOf(newEvidenceIds);
        this.updatedAt = now != null ? now : Instant.now();
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public String domainName() { return domainName; }
    public RootCause rootCause() { return rootCause; }
    public Sender sender() { return sender; }
    public String originHealthSignalId() { return originHealthSignalId; }
    public List<String> originDmarcAggregateEvidenceIds() { return originDmarcAggregateEvidenceIds; }
    public String latestHealthSignalId() { return latestHealthSignalId; }
    public List<String> latestDmarcAggregateEvidenceIds() { return latestDmarcAggregateEvidenceIds; }
    public IssueStatus status() { return status; }
    public Instant discoveredAt() { return discoveredAt; }
    public Instant resolvedAt() { return resolvedAt; }
    public Instant closedAt() { return closedAt; }
    public Instant updatedAt() { return updatedAt; }

    public boolean isActive() {
        return status == IssueStatus.DISCOVERED || status == IssueStatus.DIAGNOSED || status == IssueStatus.RESOLVED;
    }
}
