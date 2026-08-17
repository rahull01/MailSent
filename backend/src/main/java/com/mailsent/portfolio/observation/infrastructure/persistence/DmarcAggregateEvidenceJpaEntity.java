package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.LocalDate;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * DmarcAggregateEvidenceJpaEntity — durable persistence mapping for
 * {@link DmarcAggregateEvidence}. A persistence concern, not a duplicate
 * domain model. {@code dkim_domains} is stored as a TEXT JSON array;
 * {@code message_count} is the RFC 7489 record {@code count}.
 */
@Entity
@Table(name = "dmarc_aggregate_evidence")
public class DmarcAggregateEvidenceJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "receipt_id", length = 36, nullable = false)
    private String receiptId;

    @Column(name = "report_id", nullable = false, columnDefinition = "TEXT")
    private String reportId;

    @Column(name = "source_ip", columnDefinition = "TEXT")
    private String sourceIp;

    @Column(name = "utc_day")
    private LocalDate utcDay;

    @Column(name = "message_count")
    private Long messageCount;

    @Column(name = "spf_result", columnDefinition = "TEXT")
    private String spfResult;

    @Column(name = "dkim_result", columnDefinition = "TEXT")
    private String dkimResult;

    @Column(name = "spf_aligned", columnDefinition = "TEXT")
    private String spfAligned;

    @Column(name = "dkim_aligned", columnDefinition = "TEXT")
    private String dkimAligned;

    @Column(name = "envelope_from", columnDefinition = "TEXT")
    private String envelopeFrom;

    @Column(name = "header_from", columnDefinition = "TEXT")
    private String headerFrom;

    @Column(name = "dkim_domains", nullable = false, columnDefinition = "TEXT")
    private String dkimDomains;

    @Column(name = "disposition", columnDefinition = "TEXT")
    private String disposition;

    protected DmarcAggregateEvidenceJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public DmarcAggregateEvidenceJpaEntity(String id, String organizationId, String domainId, String domainName,
                                           String receiptId, String reportId, String sourceIp, LocalDate utcDay,
                                           Long messageCount, String spfResult, String dkimResult, String spfAligned,
                                           String dkimAligned, String envelopeFrom, String headerFrom,
                                           String dkimDomains, String disposition) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.receiptId = receiptId;
        this.reportId = reportId;
        this.sourceIp = sourceIp;
        this.utcDay = utcDay;
        this.messageCount = messageCount;
        this.spfResult = spfResult;
        this.dkimResult = dkimResult;
        this.spfAligned = spfAligned;
        this.dkimAligned = dkimAligned;
        this.envelopeFrom = envelopeFrom;
        this.headerFrom = headerFrom;
        this.dkimDomains = dkimDomains;
        this.disposition = disposition;
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

    public String receiptId() {
        return receiptId;
    }

    public String reportId() {
        return reportId;
    }

    public String sourceIp() {
        return sourceIp;
    }

    public LocalDate utcDay() {
        return utcDay;
    }

    public Long messageCount() {
        return messageCount;
    }

    public String spfResult() {
        return spfResult;
    }

    public String dkimResult() {
        return dkimResult;
    }

    public String spfAligned() {
        return spfAligned;
    }

    public String dkimAligned() {
        return dkimAligned;
    }

    public String envelopeFrom() {
        return envelopeFrom;
    }

    public String headerFrom() {
        return headerFrom;
    }

    public String dkimDomains() {
        return dkimDomains;
    }

    public String disposition() {
        return disposition;
    }
}
