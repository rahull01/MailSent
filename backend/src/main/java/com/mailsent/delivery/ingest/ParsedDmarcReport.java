package com.mailsent.delivery.ingest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ParsedDmarcReport — Structured aggregate RUA report model (INT-004).
 */
public class ParsedDmarcReport {

    private final String reportId;
    private final String orgName;
    private final String email;
    private final Instant dateRangeBegin;
    private final Instant dateRangeEnd;
    private final List<ParsedDmarcRecord> records;

    public ParsedDmarcReport(String reportId, String orgName, String email,
                             Instant dateRangeBegin, Instant dateRangeEnd,
                             List<ParsedDmarcRecord> records) {
        this.reportId = Objects.requireNonNull(reportId, "report_id is strictly required (INT-004)");
        this.orgName = Objects.requireNonNull(orgName);
        this.email = email;
        this.dateRangeBegin = Objects.requireNonNull(dateRangeBegin);
        this.dateRangeEnd = Objects.requireNonNull(dateRangeEnd);
        this.records = Collections.unmodifiableList(Objects.requireNonNull(records));
    }

    public String reportId() { return reportId; }
    public String orgName() { return orgName; }
    public String email() { return email; }
    public Instant dateRangeBegin() { return dateRangeBegin; }
    public Instant dateRangeEnd() { return dateRangeEnd; }
    public List<ParsedDmarcRecord> records() { return records; }
}
