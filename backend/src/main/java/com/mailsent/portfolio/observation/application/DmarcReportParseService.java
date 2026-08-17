package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;

/**
 * DmarcReportParseService — the INT-004 application boundary.
 *
 * <p>Parses a stored {@link DmarcReportReceipt}'s opaque bytes into an
 * in-memory {@link ParsedDmarcReport}. It is invoked explicitly; it is not an
 * HTTP endpoint, not a scheduler, and is not called from INT-003 receive.
 * Successful parse associates extracted {@code report_id} with the originating
 * Receipt. Unusable input produces no parsed report, no aggregate snapshot,
 * and no Observation Evidence / Alignment / Health Signal writes.</p>
 */
public class DmarcReportParseService {

    private final DmarcReportReceiptRepository receipts;
    private final DmarcAggregateXmlParser parser;

    public DmarcReportParseService(DmarcReportReceiptRepository receipts, DmarcAggregateXmlParser parser) {
        this.receipts = receipts;
        this.parser = parser;
    }

    /**
     * Parses the stored receipt identified by {@code receiptId}.
     *
     * @param receiptId stable identity of an already-received receipt
     * @return usable parsed facts, or unusable when the receipt is missing or
     *         the payload is not a valid RFC 7489 aggregate with {@code report_id}
     */
    public DmarcReportParseOutcome parse(String receiptId) {
        if (receiptId == null || receiptId.isBlank()) {
            return DmarcReportParseOutcome.unusable(DmarcReportParseFailure.MISSING_RECEIPT);
        }
        DmarcReportReceipt receipt = receipts.findById(receiptId).orElse(null);
        if (receipt == null) {
            return DmarcReportParseOutcome.unusable(DmarcReportParseFailure.MISSING_RECEIPT);
        }
        DmarcAggregateXmlExtraction extraction = parser.extract(receipt.payload());
        if (!extraction.extracted()) {
            return DmarcReportParseOutcome.unusable(extraction.failure());
        }
        ParsedDmarcReport parsed = ParsedDmarcReport.fromReceipt(
            receipt,
            extraction.reportId(),
            extraction.reportingOrg(),
            extraction.dateRangeBegin(),
            extraction.dateRangeEnd(),
            extraction.policyDomain(),
            extraction.policyDisposition(),
            extraction.policyDkim(),
            extraction.policySpf(),
            extraction.records());
        receipts.associateReportId(receipt.id(), parsed.reportId());
        return DmarcReportParseOutcome.parsed(parsed);
    }
}
