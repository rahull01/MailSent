package com.mailsent.portfolio.observation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DmarcAggregateEvidence — an immutable persisted aggregated RUA snapshot
 * for a registered Domain (INT-005).
 *
 * <p>Owned by Delivery Observation. It is not an Alignment table, not
 * Observation Evidence, and not Health Signal. Tenant and domain identity
 * come from the originating {@link ParsedDmarcReport} (itself bound to
 * {@link DmarcReportReceipt}), never from XML {@code policy_published/domain}.
 * One snapshot is one {@code source_ip} on one UTC day of one report.</p>
 *
 * <p>Factory-based creation only ({@link #fromParsed}, {@link #restore}).
 * No setters, no update, and no delete. A later ingest of a different
 * {@code report_id} is a new snapshot. Duplicate {@code report_id} for the
 * same Domain is handled by the application service (idempotent, no second
 * snapshot).</p>
 */
public final class DmarcAggregateEvidence {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final String receiptId;
    private final String reportId;
    private final String sourceIp;
    private final LocalDate utcDay;
    private final Long count;
    private final String spfResult;
    private final String dkimResult;
    private final String spfAligned;
    private final String dkimAligned;
    private final String envelopeFrom;
    private final String headerFrom;
    private final List<String> dkimDomains;
    private final String disposition;

    private DmarcAggregateEvidence(String id, String organizationId, String domainId, String domainName,
                                   String receiptId, String reportId, String sourceIp, LocalDate utcDay,
                                   Long count, String spfResult, String dkimResult, String spfAligned,
                                   String dkimAligned, String envelopeFrom, String headerFrom,
                                   List<String> dkimDomains, String disposition) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.receiptId = receiptId;
        this.reportId = reportId;
        this.sourceIp = sourceIp;
        this.utcDay = utcDay;
        this.count = count;
        this.spfResult = spfResult;
        this.dkimResult = dkimResult;
        this.spfAligned = spfAligned;
        this.dkimAligned = dkimAligned;
        this.envelopeFrom = envelopeFrom;
        this.headerFrom = headerFrom;
        this.dkimDomains = dkimDomains == null ? List.of() : List.copyOf(dkimDomains);
        this.disposition = disposition;
    }

    /**
     * Builds one per-{@code source_ip} snapshot from a usable parsed report.
     * Identity is taken from {@code parsed} (Receipt lineage). Record facts
     * are stored as supplied; this factory does not derive Alignment.
     */
    public static DmarcAggregateEvidence fromParsed(ParsedDmarcReport parsed, String sourceIp, LocalDate utcDay,
                                                    Long count, String spfResult, String dkimResult,
                                                    String spfAligned, String dkimAligned, String envelopeFrom,
                                                    String headerFrom, List<String> dkimDomains,
                                                    String disposition) {
        if (parsed == null) {
            throw new IllegalArgumentException("A parsed DMARC report is required.");
        }
        if (parsed.reportId() == null || parsed.reportId().isBlank()) {
            throw new IllegalArgumentException("report_id is required to store DMARC Aggregate Evidence.");
        }
        return new DmarcAggregateEvidence(
            UUID.randomUUID().toString(),
            parsed.organizationId(),
            parsed.domainId(),
            parsed.domainName(),
            parsed.receiptId(),
            parsed.reportId(),
            sourceIp,
            utcDay,
            count,
            spfResult,
            dkimResult,
            spfAligned,
            dkimAligned,
            envelopeFrom,
            headerFrom,
            dkimDomains,
            disposition);
    }

    /**
     * Reconstructs a snapshot from durable storage, adopting stored identity
     * and facts verbatim.
     */
    public static DmarcAggregateEvidence restore(String id, String organizationId, String domainId, String domainName,
                                                 String receiptId, String reportId, String sourceIp, LocalDate utcDay,
                                                 Long count, String spfResult, String dkimResult, String spfAligned,
                                                 String dkimAligned, String envelopeFrom, String headerFrom,
                                                 List<String> dkimDomains, String disposition) {
        return new DmarcAggregateEvidence(
            id, organizationId, domainId, domainName, receiptId, reportId, sourceIp, utcDay, count,
            spfResult, dkimResult, spfAligned, dkimAligned, envelopeFrom, headerFrom, dkimDomains, disposition);
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

    public Long count() {
        return count;
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

    public List<String> dkimDomains() {
        return dkimDomains;
    }

    public String disposition() {
        return disposition;
    }
}
