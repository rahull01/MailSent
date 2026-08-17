package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.ReputationObservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ReputationObservationJpaEntity — durable persistence mapping for
 * {@link ReputationObservation}. A persistence concern, not a duplicate domain
 * model. The raw provider payload is stored as TEXT.
 */
@Entity
@Table(name = "reputation_observation")
public class ReputationObservationJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "source", length = 32, nullable = false)
    private String source;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    protected ReputationObservationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public ReputationObservationJpaEntity(String id, String organizationId, String domainId, String domainName,
                                          String source, Instant observedAt, String payload) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.source = source;
        this.observedAt = observedAt;
        this.payload = payload;
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

    public String source() {
        return source;
    }

    public Instant observedAt() {
        return observedAt;
    }

    public String payload() {
        return payload;
    }
}
