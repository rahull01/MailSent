package com.mailsent.diagnosis.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mailsent.diagnosis.domain.Alignment;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.application.DmarcIngestDiag001Lineage;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * AlignmentDerivationService — DIAG-001 Analyzer-class Alignment derivation.
 *
 * <p>Loads {@link DmarcAggregateEvidence} by INV-11 lineage ids from INT-006
 * and presents each snapshot as a separate in-memory {@link Alignment}. It
 * does not persist Alignment, mutate Observation Evidence, create Health
 * Signal, or recompute SPF/DKIM alignment from identities.</p>
 */
public class AlignmentDerivationService {

    private final DmarcAggregateEvidenceRepository evidence;

    private volatile List<Alignment> lastDerived = List.of();

    public AlignmentDerivationService(DmarcAggregateEvidenceRepository evidence) {
        this.evidence = evidence;
    }

    /**
     * Derives one Alignment per lineage evidence id that loads and matches the
     * trigger lineage. Absent snapshots are omitted (not fabricated). Conflicting
     * rows are not last-win merged.
     */
    public List<Alignment> derive(DmarcIngestDiag001Lineage lineage) {
        if (lineage == null) {
            throw new IllegalArgumentException("DIAG-001 lineage is required.");
        }
        List<Alignment> derived = new ArrayList<>();
        for (String evidenceId : lineage.evidenceIds()) {
            Optional<DmarcAggregateEvidence> found = evidence.findById(evidenceId);
            if (found.isEmpty()) {
                continue;
            }
            DmarcAggregateEvidence snapshot = found.get();
            if (!matchesLineage(snapshot, lineage)) {
                continue;
            }
            derived.add(Alignment.fromEvidence(snapshot));
        }
        lastDerived = List.copyOf(derived);
        return lastDerived;
    }

    /** Most recent in-memory derivation from {@link #derive}; never persisted. */
    public List<Alignment> lastDerived() {
        return lastDerived;
    }

    private static boolean matchesLineage(DmarcAggregateEvidence snapshot, DmarcIngestDiag001Lineage lineage) {
        return Objects.equals(snapshot.receiptId(), lineage.receiptId())
            && Objects.equals(snapshot.reportId(), lineage.reportId())
            && Objects.equals(snapshot.domainId(), lineage.domainId())
            && Objects.equals(snapshot.organizationId(), lineage.organizationId());
    }
}
