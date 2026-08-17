package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;

/**
 * DmarcIngestTriggerService — the INT-006 application boundary.
 *
 * <p>Explicit in-process step after receive: parse (INT-004) → store (INT-005)
 * → trigger DIAG-001 once when a <em>new</em> DMARC Aggregate Evidence snapshot
 * is persisted. It is not an HTTP endpoint, not a scheduler, and is not called
 * from the INT-003 webhook. Call {@link #process(String)} on a receipt after
 * receive (tests, operators, or a later INT). INF-004 is not extended.</p>
 *
 * <p>INT-005 {@code store} remains trigger-free. Duplicate {@code report_id} +
 * Domain is an idempotent skip and must not re-trigger. Unusable parse and
 * failed store must not trigger. DIAG-002, DIAG-003, HEALTH-001, and MAP-001–004
 * are out of scope.</p>
 */
public class DmarcIngestTriggerService {

    private final DmarcReportParseService parse;
    private final DmarcAggregateEvidenceService store;
    private final DmarcAggregateEvidenceRepository evidence;
    private final Diag001AlignmentDerivationPort diag001;

    public DmarcIngestTriggerService(DmarcReportParseService parse,
                                     DmarcAggregateEvidenceService store,
                                     DmarcAggregateEvidenceRepository evidence,
                                     Diag001AlignmentDerivationPort diag001) {
        this.parse = parse;
        this.store = store;
        this.evidence = java.util.Objects.requireNonNull(evidence);
        this.diag001 = diag001;
    }

    /**
     * Parses and stores the receipt, then triggers DIAG-001 only when new
     * aggregate evidence was persisted for this {@code report_id} + Domain.
     */
    public void process(String receiptId) {
        DmarcReportParseOutcome outcome = parse.parse(receiptId);
        if (!outcome.usable()) {
            return;
        }
        ParsedDmarcReport parsed = outcome.parsed().orElseThrow();
        DmarcAggregateStoreResult stored = store.storeResult(parsed);
        if (!stored.createdNew() || stored.snapshots().isEmpty()) {
            return;
        }
        diag001.trigger(DmarcIngestDiag001Lineage.from(parsed, stored.snapshots()));
    }
}
