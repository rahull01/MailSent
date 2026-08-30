package com.mailsent.diagnosis.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issues")
public class IssueJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "root_cause", length = 64, nullable = false)
    private String rootCause;

    @Column(name = "provider_id", length = 64)
    private String providerId;

    @Column(name = "provider_name", length = 128)
    private String providerName;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "origin_health_signal_id", length = 36, nullable = false)
    private String originHealthSignalId;

    @Column(name = "origin_dmarc_aggregate_evidence_ids", columnDefinition = "TEXT", nullable = false)
    private String originDmarcAggregateEvidenceIds;

    @Column(name = "latest_health_signal_id", length = 36, nullable = false)
    private String latestHealthSignalId;

    @Column(name = "latest_dmarc_aggregate_evidence_ids", columnDefinition = "TEXT", nullable = false)
    private String latestDmarcAggregateEvidenceIds;

    @Column(name = "discovered_at", nullable = false)
    private Instant discoveredAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public IssueJpaEntity() {}

    public IssueJpaEntity(String id, String organizationId, String domainId, String domainName,
                          String rootCause, String providerId, String providerName, String status,
                          String originHealthSignalId, String originDmarcAggregateEvidenceIds,
                          String latestHealthSignalId, String latestDmarcAggregateEvidenceIds,
                          Instant discoveredAt, Instant resolvedAt, Instant closedAt, Instant updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.rootCause = rootCause;
        this.providerId = providerId;
        this.providerName = providerName;
        this.status = status;
        this.originHealthSignalId = originHealthSignalId;
        this.originDmarcAggregateEvidenceIds = originDmarcAggregateEvidenceIds;
        this.latestHealthSignalId = latestHealthSignalId;
        this.latestDmarcAggregateEvidenceIds = latestDmarcAggregateEvidenceIds;
        this.discoveredAt = discoveredAt;
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getOrganizationId() { return organizationId; }
    public String getDomainId() { return domainId; }
    public String getDomainName() { return domainName; }
    public String getRootCause() { return rootCause; }
    public String getProviderId() { return providerId; }
    public String getProviderName() { return providerName; }
    public String getStatus() { return status; }
    public String getOriginHealthSignalId() { return originHealthSignalId; }
    public String getOriginDmarcAggregateEvidenceIds() { return originDmarcAggregateEvidenceIds; }
    public String getLatestHealthSignalId() { return latestHealthSignalId; }
    public String getLatestDmarcAggregateEvidenceIds() { return latestDmarcAggregateEvidenceIds; }
    public Instant getDiscoveredAt() { return discoveredAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Instant getClosedAt() { return closedAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setLatestHealthSignalId(String latestHealthSignalId) { this.latestHealthSignalId = latestHealthSignalId; }
    public void setLatestDmarcAggregateEvidenceIds(String latestDmarcAggregateEvidenceIds) { this.latestDmarcAggregateEvidenceIds = latestDmarcAggregateEvidenceIds; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
