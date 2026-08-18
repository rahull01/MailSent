package com.mailsent.delivery.ingest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * DmarcAggregateEvidence — Immutable snapshot of aggregated RUA evidence (INT-005).
 */
public class DmarcAggregateEvidence {

    private final String id;
    private final String domainId;
    private final String organizationId;
    private final String reportId;
    private final String receiptId;
    private final LocalDate calendarDateUtc;
    private final int totalMessages;
    private final int fullyCompliantMessages;
    private final Map<String, Integer> volumeBySourceIp;
    private final Instant generatedAt;

    private DmarcAggregateEvidence(String id, String domainId, String organizationId,
                                   String reportId, String receiptId,
                                   LocalDate calendarDateUtc, int totalMessages,
                                   int fullyCompliantMessages,
                                   Map<String, Integer> volumeBySourceIp,
                                   Instant generatedAt) {
        this.id = Objects.requireNonNull(id);
        this.domainId = Objects.requireNonNull(domainId);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.reportId = Objects.requireNonNull(reportId);
        this.receiptId = Objects.requireNonNull(receiptId);
        this.calendarDateUtc = Objects.requireNonNull(calendarDateUtc);
        this.totalMessages = totalMessages;
        this.fullyCompliantMessages = fullyCompliantMessages;
        this.volumeBySourceIp = Collections.unmodifiableMap(Objects.requireNonNull(volumeBySourceIp));
        this.generatedAt = Objects.requireNonNull(generatedAt);
    }

    public static DmarcAggregateEvidence aggregate(DmarcReportReceipt receipt, ParsedDmarcReport report) {
        Objects.requireNonNull(receipt);
        Objects.requireNonNull(report);

        LocalDate dateUtc = report.dateRangeBegin().atZone(ZoneOffset.UTC).toLocalDate();

        int total = 0;
        int pass = 0;
        Map<String, Integer> ipMap = new HashMap<>();

        for (ParsedDmarcRecord rec : report.records()) {
            total += rec.count();
            if (rec.isDkimAligned() && rec.isSpfAligned()) {
                pass += rec.count();
            }
            ipMap.put(rec.sourceIp(), ipMap.getOrDefault(rec.sourceIp(), 0) + rec.count());
        }

        return new DmarcAggregateEvidence(
                UUID.randomUUID().toString(),
                receipt.domainId(),
                receipt.organizationId(),
                report.reportId(),
                receipt.id(),
                dateUtc,
                total,
                pass,
                ipMap,
                Instant.now()
        );
    }

    public String id() { return id; }
    public String domainId() { return domainId; }
    public String organizationId() { return organizationId; }
    public String reportId() { return reportId; }
    public String receiptId() { return receiptId; }
    public LocalDate calendarDateUtc() { return calendarDateUtc; }
    public int totalMessages() { return totalMessages; }
    public int fullyCompliantMessages() { return fullyCompliantMessages; }
    public Map<String, Integer> volumeBySourceIp() { return volumeBySourceIp; }
    public Instant generatedAt() { return generatedAt; }
}
