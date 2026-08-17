package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * ParsedDmarcReport — raw facts extracted from a valid RFC 7489 aggregate
 * XML report (INT-004).
 *
 * <p>Owned by Delivery Observation. It is not Alignment, not Observation
 * Evidence, not Health Signal, and not DMARC Aggregate Evidence (INT-005).
 * It is an in-memory parsed representation: architecture does not authorize
 * a separate parsed-report table.</p>
 *
 * <p>Tenant and domain identity are taken from the originating
 * {@link DmarcReportReceipt}, never from XML {@code policy_published/domain}.
 * {@code report_id} is required; a report without it is not a valid parsed
 * report.</p>
 */
public final class ParsedDmarcReport {

    private final String receiptId;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final String reportId;
    private final String reportingOrg;
    private final Instant dateRangeBegin;
    private final Instant dateRangeEnd;
    private final String policyDomain;
    private final String policyDisposition;
    private final String policyDkim;
    private final String policySpf;
    private final List<ParsedDmarcRecord> records;

    private ParsedDmarcReport(String receiptId, String organizationId, String domainId, String domainName,
                              String reportId, String reportingOrg, Instant dateRangeBegin, Instant dateRangeEnd,
                              String policyDomain, String policyDisposition, String policyDkim, String policySpf,
                              List<ParsedDmarcRecord> records) {
        this.receiptId = receiptId;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.reportId = reportId;
        this.reportingOrg = reportingOrg;
        this.dateRangeBegin = dateRangeBegin == null ? null : dateRangeBegin.truncatedTo(ChronoUnit.MICROS);
        this.dateRangeEnd = dateRangeEnd == null ? null : dateRangeEnd.truncatedTo(ChronoUnit.MICROS);
        this.policyDomain = policyDomain;
        this.policyDisposition = policyDisposition;
        this.policyDkim = policyDkim;
        this.policySpf = policySpf;
        this.records = List.copyOf(records);
    }

    /**
     * Builds a parsed report whose identity is the originating Receipt and
     * whose facts are the extracted XML values. {@code reportId} must be
     * present (already extracted); this factory does not fabricate it.
     */
    public static ParsedDmarcReport fromReceipt(DmarcReportReceipt receipt, String reportId, String reportingOrg,
                                                Instant dateRangeBegin, Instant dateRangeEnd, String policyDomain,
                                                String policyDisposition, String policyDkim, String policySpf,
                                                List<ParsedDmarcRecord> records) {
        if (receipt == null) {
            throw new IllegalArgumentException("An originating receipt is required.");
        }
        if (reportId == null || reportId.isBlank()) {
            throw new IllegalArgumentException("report_id is required to accept as a Parsed DMARC Report.");
        }
        List<ParsedDmarcRecord> recordFacts = records == null ? List.of() : records;
        return new ParsedDmarcReport(
            receipt.id(),
            receipt.organizationId(),
            receipt.domainId(),
            receipt.domainName(),
            reportId.trim(),
            blankToNull(reportingOrg),
            dateRangeBegin,
            dateRangeEnd,
            blankToNull(policyDomain),
            blankToNull(policyDisposition),
            blankToNull(policyDkim),
            blankToNull(policySpf),
            recordFacts);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String receiptId() {
        return receiptId;
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

    public String reportingOrg() {
        return reportingOrg;
    }

    public Instant dateRangeBegin() {
        return dateRangeBegin;
    }

    public Instant dateRangeEnd() {
        return dateRangeEnd;
    }

    public String policyDomain() {
        return policyDomain;
    }

    public String policyDisposition() {
        return policyDisposition;
    }

    public String policyDkim() {
        return policyDkim;
    }

    public String policySpf() {
        return policySpf;
    }

    public List<ParsedDmarcRecord> records() {
        return records;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ParsedDmarcReport that)) {
            return false;
        }
        return Objects.equals(receiptId, that.receiptId)
            && Objects.equals(reportId, that.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId, reportId);
    }
}
