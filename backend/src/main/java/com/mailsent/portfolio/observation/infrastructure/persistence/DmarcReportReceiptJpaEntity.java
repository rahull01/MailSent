package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * DmarcReportReceiptJpaEntity — durable persistence mapping for
 * {@link DmarcReportReceipt}. A persistence concern, not a duplicate domain
 * model. Payload is BYTEA; report_id is nullable TEXT.
 */
@Entity
@Table(name = "dmarc_report_receipt")
public class DmarcReportReceiptJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Column(name = "domain_name", length = 253, nullable = false)
    private String domainName;

    @Column(name = "report_id", columnDefinition = "TEXT")
    private String reportId;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "payload", nullable = false, columnDefinition = "BYTEA")
    private byte[] payload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected DmarcReportReceiptJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public DmarcReportReceiptJpaEntity(String id, String organizationId, String domainId, String domainName,
                                       String reportId, byte[] payload, Instant receivedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.reportId = reportId;
        this.payload = payload;
        this.receivedAt = receivedAt;
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

    public String reportId() {
        return reportId;
    }

    public byte[] payload() {
        return payload;
    }

    public Instant receivedAt() {
        return receivedAt;
    }
}
