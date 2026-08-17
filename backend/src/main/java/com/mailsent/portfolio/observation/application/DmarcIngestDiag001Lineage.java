package com.mailsent.portfolio.observation.application;

import java.util.List;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;

/**
 * INV-11 lineage passed to DIAG-001 after a successful new INT-005 store
 * (INT-006). Enough to locate the Receipt and DMARC Aggregate Evidence later;
 * this type does not carry Alignment, Health Signal, or Trust Score.
 */
public record DmarcIngestDiag001Lineage(
    String receiptId,
    String reportId,
    String domainId,
    String organizationId,
    List<String> evidenceIds) {

    public DmarcIngestDiag001Lineage {
        evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
    }

    static DmarcIngestDiag001Lineage from(ParsedDmarcReport parsed, List<DmarcAggregateEvidence> stored) {
        List<String> ids = stored.stream().map(DmarcAggregateEvidence::id).toList();
        return new DmarcIngestDiag001Lineage(
            parsed.receiptId(),
            parsed.reportId(),
            parsed.domainId(),
            parsed.organizationId(),
            ids);
    }
}
