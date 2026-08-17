package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * DmarcAggregateEvidenceRepository — the port for persisting and retrieving
 * DMARC Aggregate Evidence (INT-005).
 *
 * <p>Insert-only: there is no update or delete. Duplicate {@code report_id}
 * for the same Domain is occupancy of {@code domain_id} + {@code report_id}
 * (not UNIQUE on evidence rows, which may include multiple {@code source_ip}
 * snapshots). {@link #persistNewSnapshotSet} occupies then inserts all rows
 * atomically, or returns the existing set.</p>
 */
public interface DmarcAggregateEvidenceRepository {

    /** Persists a new snapshot and returns it. */
    DmarcAggregateEvidence save(DmarcAggregateEvidence evidence);

    /**
     * Occupies {@code domain_id} + {@code report_id} and inserts every row,
     * or returns the existing snapshot set if already occupied. All rows of
     * a new set are committed together, or none are.
     */
    DmarcAggregateStoreResult persistNewSnapshotSet(List<DmarcAggregateEvidence> snapshots);

    /** Loads a snapshot by its stable identity, if present. */
    Optional<DmarcAggregateEvidence> findById(String id);

    /** Lists all snapshots captured for a single registered domain. */
    List<DmarcAggregateEvidence> findByDomainId(String domainId);

    /** Lists all snapshots belonging to a single organization. */
    List<DmarcAggregateEvidence> findByOrganizationId(String organizationId);

    /**
     * Lists snapshots already stored for this Domain and {@code report_id}.
     * Presence of any row means the whole report was already stored.
     */
    List<DmarcAggregateEvidence> findByDomainIdAndReportId(String domainId, String reportId);
}
