package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.DnsHealthObservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * DnsHealthObservationJpaEntity — durable persistence mapping for
 * {@link DnsHealthObservation}. A persistence concern, not a duplicate domain
 * model. TTL, DNSSEC, and propagation are stored as TEXT JSON; the adapter
 * alone serializes and deserializes them.
 */
@Entity
@Table(name = "dns_health_observation")
public class DnsHealthObservationJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "ttl_observations", nullable = false, columnDefinition = "TEXT")
    private String ttlObservations;

    @Column(name = "dnssec_observations", nullable = false, columnDefinition = "TEXT")
    private String dnssecObservations;

    @Column(name = "propagation_observations", nullable = false, columnDefinition = "TEXT")
    private String propagationObservations;

    protected DnsHealthObservationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public DnsHealthObservationJpaEntity(String id, String organizationId, String domainId, String domainName,
                                         Instant observedAt, String ttlObservations, String dnssecObservations,
                                         String propagationObservations) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.observedAt = observedAt;
        this.ttlObservations = ttlObservations;
        this.dnssecObservations = dnssecObservations;
        this.propagationObservations = propagationObservations;
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

    public Instant observedAt() {
        return observedAt;
    }

    public String ttlObservations() {
        return ttlObservations;
    }

    public String dnssecObservations() {
        return dnssecObservations;
    }

    public String propagationObservations() {
        return propagationObservations;
    }
}
