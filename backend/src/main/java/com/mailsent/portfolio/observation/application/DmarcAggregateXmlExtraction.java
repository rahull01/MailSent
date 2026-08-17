package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.util.List;

import com.mailsent.portfolio.observation.domain.ParsedDmarcRecord;

/**
 * Result of reading opaque receipt bytes as RFC 7489 aggregate XML.
 * Success carries extracted facts; failure carries a parse failure only.
 */
public final class DmarcAggregateXmlExtraction {

    private final String reportId;
    private final String reportingOrg;
    private final Instant dateRangeBegin;
    private final Instant dateRangeEnd;
    private final String policyDomain;
    private final String policyDisposition;
    private final String policyDkim;
    private final String policySpf;
    private final List<ParsedDmarcRecord> records;
    private final DmarcReportParseFailure failure;

    private DmarcAggregateXmlExtraction(String reportId, String reportingOrg, Instant dateRangeBegin,
                                        Instant dateRangeEnd, String policyDomain, String policyDisposition,
                                        String policyDkim, String policySpf, List<ParsedDmarcRecord> records,
                                        DmarcReportParseFailure failure) {
        this.reportId = reportId;
        this.reportingOrg = reportingOrg;
        this.dateRangeBegin = dateRangeBegin;
        this.dateRangeEnd = dateRangeEnd;
        this.policyDomain = policyDomain;
        this.policyDisposition = policyDisposition;
        this.policyDkim = policyDkim;
        this.policySpf = policySpf;
        this.records = records == null ? List.of() : List.copyOf(records);
        this.failure = failure;
    }

    public static DmarcAggregateXmlExtraction extracted(String reportId, String reportingOrg,
                                                        Instant dateRangeBegin, Instant dateRangeEnd,
                                                        String policyDomain, String policyDisposition,
                                                        String policyDkim, String policySpf,
                                                        List<ParsedDmarcRecord> records) {
        return new DmarcAggregateXmlExtraction(
            reportId, reportingOrg, dateRangeBegin, dateRangeEnd, policyDomain, policyDisposition,
            policyDkim, policySpf, records, null);
    }

    public static DmarcAggregateXmlExtraction unusable(DmarcReportParseFailure failure) {
        return new DmarcAggregateXmlExtraction(null, null, null, null, null, null, null, null, List.of(), failure);
    }

    public boolean extracted() {
        return failure == null;
    }

    public DmarcReportParseFailure failure() {
        return failure;
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
}
