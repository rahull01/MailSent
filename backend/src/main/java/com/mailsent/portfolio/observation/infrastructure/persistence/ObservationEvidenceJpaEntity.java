package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.ObservationEvidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ObservationEvidenceJpaEntity — durable persistence mapping for {@link ObservationEvidence}.
 * A persistence concern, not a duplicate domain model. MX and TXT are stored as TEXT JSON
 * arrays; the adapter alone serializes and deserializes them.
 */
@Entity
@Table(name = "observation_evidence")
public class ObservationEvidenceJpaEntity {

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

    @Column(name = "mx_records", nullable = false, columnDefinition = "TEXT")
    private String mxRecords;

    @Column(name = "txt_records", nullable = false, columnDefinition = "TEXT")
    private String txtRecords;

    @Column(name = "spf_record", columnDefinition = "TEXT")
    private String spfRecord;

    protected ObservationEvidenceJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public ObservationEvidenceJpaEntity(String id, String organizationId, String domainId, String domainName,
                                        Instant observedAt, String mxRecords, String txtRecords, String spfRecord) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.observedAt = observedAt;
        this.mxRecords = mxRecords;
        this.txtRecords = txtRecords;
        this.spfRecord = spfRecord;
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

    public String mxRecords() {
        return mxRecords;
    }

    public String txtRecords() {
        return txtRecords;
    }

    public String spfRecord() {
        return spfRecord;
    }
}
