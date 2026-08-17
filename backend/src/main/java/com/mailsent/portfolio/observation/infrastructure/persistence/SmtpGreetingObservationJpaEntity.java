package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * SmtpGreetingObservationJpaEntity — durable persistence mapping for
 * {@link SmtpGreetingObservation}. A persistence concern, not a duplicate domain
 * model. Greeting and EHLO are stored as nullable TEXT.
 */
@Entity
@Table(name = "smtp_greeting_observation")
public class SmtpGreetingObservationJpaEntity {

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

    @Column(name = "probe_outcome", length = 16, nullable = false)
    private String probeOutcome;

    @Column(name = "raw_greeting", columnDefinition = "TEXT")
    private String rawGreeting;

    @Column(name = "raw_ehlo", columnDefinition = "TEXT")
    private String rawEhlo;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    protected SmtpGreetingObservationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public SmtpGreetingObservationJpaEntity(String id, String organizationId, String domainId, String domainName,
                                            String sourceMxHostname, String ip, String addressFamily,
                                            String probeOutcome, String rawGreeting, String rawEhlo,
                                            Instant observedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.sourceMxHostname = sourceMxHostname;
        this.ip = ip;
        this.addressFamily = addressFamily;
        this.probeOutcome = probeOutcome;
        this.rawGreeting = rawGreeting;
        this.rawEhlo = rawEhlo;
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

    public String probeOutcome() {
        return probeOutcome;
    }

    public String rawGreeting() {
        return rawGreeting;
    }

    public String rawEhlo() {
        return rawEhlo;
    }

    public Instant observedAt() {
        return observedAt;
    }
}
