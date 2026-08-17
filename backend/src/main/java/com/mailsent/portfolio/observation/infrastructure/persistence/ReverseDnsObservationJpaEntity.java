package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ReverseDnsObservationJpaEntity — durable persistence mapping for
 * {@link ReverseDnsObservation}. A persistence concern, not a duplicate domain
 * model. PTR values are stored as TEXT JSON; the adapter alone serializes and
 * deserializes them.
 */
@Entity
@Table(name = "reverse_dns_observation")
public class ReverseDnsObservationJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "source_mx_hostname", length = 253, nullable = false)
    private String sourceMxHostname;

    @Column(name = "ip", length = 45, nullable = false)
    private String ip;

    @Column(name = "address_family", length = 8, nullable = false)
    private String addressFamily;

    @Column(name = "ptr_values", nullable = false, columnDefinition = "TEXT")
    private String ptrValues;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    protected ReverseDnsObservationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public ReverseDnsObservationJpaEntity(String id, String organizationId, String domainId, String domainName,
                                          String sourceMxHostname, String ip, String addressFamily,
                                          String ptrValues, Instant observedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.sourceMxHostname = sourceMxHostname;
        this.ip = ip;
        this.addressFamily = addressFamily;
        this.ptrValues = ptrValues;
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

    public String sourceMxHostname() {
        return sourceMxHostname;
    }

    public String ip() {
        return ip;
    }

    public String addressFamily() {
        return addressFamily;
    }

    public String ptrValues() {
        return ptrValues;
    }

    public Instant observedAt() {
        return observedAt;
    }
}
