package com.mailsent.portfolio.observation.application;

import java.util.List;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * Outcome of INT-005 store: snapshots for this Domain + {@code report_id},
 * and whether this call created the occupancy (new snapshot set).
 *
 * <p>INT-006 triggers DIAG-001 only when {@link #createdNew()} is true.</p>
 */
public record DmarcAggregateStoreResult(List<DmarcAggregateEvidence> snapshots, boolean createdNew) {

    public DmarcAggregateStoreResult {
        snapshots = List.copyOf(snapshots);
    }
}
