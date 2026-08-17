package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.HealthSignal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * HealthSignalJpaEntity — durable persistence mapping for {@link HealthSignal}.
 * A persistence concern, not a duplicate domain model. Aggregate and reputation
 * lineage id lists are stored as TEXT JSON arrays; the adapter alone
 * serializes and deserializes them.
 */
@Entity
@Table(name = "health_signal")
public class HealthSignalJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "indication", nullable = false, columnDefinition = "TEXT")
    private String indication;

    @Column(name = "observation_evidence_id", length = 36, nullable = false)
    private String observationEvidenceId;

    @Column(name = "dmarc_aggregate_evidence_ids", nullable = false, columnDefinition = "TEXT")
    private String dmarcAggregateEvidenceIds;

    @Column(name = "reputation_observation_ids", nullable = false, columnDefinition = "TEXT")
    private String reputationObservationIds;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    protected HealthSignalJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public HealthSignalJpaEntity(String id, String organizationId, String domainId, String domainName,
                                 String indication, String observationEvidenceId,
                                 String dmarcAggregateEvidenceIds, String reputationObservationIds,
                                 Instant observedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.indication = indication;
        this.observationEvidenceId = observationEvidenceId;
        this.dmarcAggregateEvidenceIds = dmarcAggregateEvidenceIds;
        this.reputationObservationIds = reputationObservationIds;
        this.observedAt = observedAt;
    }

    public String id() {
        return id;
    }

    public String organizationId() {
        return organizationId;
    }

    public String domainId() {
        return domainId;
    }

    public String domainName() {
        return domainName;
    }

    public String indication() {
        return indication;
    }

    public String observationEvidenceId() {
        return observationEvidenceId;
    }

    public String dmarcAggregateEvidenceIds() {
        return dmarcAggregateEvidenceIds;
    }

    public String reputationObservationIds() {
        return reputationObservationIds;
    }

    public Instant observedAt() {
        return observedAt;
    }
}
