package com.mailsent.diagnosis.domain;

import java.util.List;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * Alignment — an in-memory derived value (DIAG-001 / FUN-H2).
 *
 * <p>It presents authentication and report-alignment facts already stored on
 * {@link DmarcAggregateEvidence}. It is not an Issue, not Health Signal, not
 * Observation Evidence, and not a persisted table. Missing evidence facts stay
 * absent ({@code null}); this type does not recompute SPF/DKIM alignment.</p>
 */
public record Alignment(
        String receiptId,
        String reportId,
        String domainId,
        String domainName,
        String organizationId,
        String evidenceId,
        String spfResult,
        String dkimResult,
        String spfAligned,
        String dkimAligned,
        String envelopeFrom,
        String headerFrom,
        List<String> dkimDomains,
        String disposition) {

    public Alignment {
        dkimDomains = dkimDomains == null ? List.of() : List.copyOf(dkimDomains);
    }

    /**
     * Derives one Alignment value from one aggregate evidence snapshot.
     * Facts are copied as stored; identities come from the snapshot (Receipt /
     * Domain), not from XML {@code policy_published.domain}.
     */
    public static Alignment fromEvidence(DmarcAggregateEvidence evidence) {
        if (evidence == null) {
            throw new IllegalArgumentException("DMARC Aggregate Evidence is required.");
        }
        return new Alignment(
            evidence.receiptId(),
            evidence.reportId(),
            evidence.domainId(),
            evidence.domainName(),
            evidence.organizationId(),
            evidence.id(),
            evidence.spfResult(),
            evidence.dkimResult(),
            evidence.spfAligned(),
            evidence.dkimAligned(),
            evidence.envelopeFrom(),
            evidence.headerFrom(),
            evidence.dkimDomains(),
            evidence.disposition());
    }
}
