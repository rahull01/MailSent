package com.mailsent.portfolio.observation.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.DmarcAggregateStoreResult;

import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * InMemoryDmarcAggregateEvidenceRepository — a hermetic, thread-safe
 * {@link DmarcAggregateEvidenceRepository} for the test profile. Snapshots are
 * keyed by their stable identity; lookups by domain, organization, or
 * domain+report_id scan the store.
 */
public class InMemoryDmarcAggregateEvidenceRepository implements DmarcAggregateEvidenceRepository {

    private final Map<String, DmarcAggregateEvidence> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> occupancy = new ConcurrentHashMap<>();
    private final Object occupancyMutex = new Object();

    @Override
    public DmarcAggregateEvidence save(DmarcAggregateEvidence evidence) {
        byId.put(evidence.id(), evidence);
        return evidence;
    }

    @Override
    public DmarcAggregateStoreResult persistNewSnapshotSet(List<DmarcAggregateEvidence> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return new DmarcAggregateStoreResult(List.of(), false);
        }
        String domainId = snapshots.get(0).domainId();
        String reportId = snapshots.get(0).reportId();
        String key = occupancyKey(domainId, reportId);
        synchronized (occupancyMutex) {
            List<DmarcAggregateEvidence> existing = findByDomainIdAndReportId(domainId, reportId);
            if (!existing.isEmpty()) {
                return new DmarcAggregateStoreResult(existing, false);
            }
            if (occupancy.putIfAbsent(key, Boolean.TRUE) != null) {
                return new DmarcAggregateStoreResult(findByDomainIdAndReportId(domainId, reportId), false);
            }
            List<DmarcAggregateEvidence> stored = new ArrayList<>();
            for (DmarcAggregateEvidence snapshot : snapshots) {
                stored.add(save(snapshot));
            }
            return new DmarcAggregateStoreResult(List.copyOf(stored), true);
        }
    }

    private static String occupancyKey(String domainId, String reportId) {
        return domainId + '\u0000' + reportId;
    }

    @Override
    public Optional<DmarcAggregateEvidence> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<DmarcAggregateEvidence> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(snapshot -> snapshot.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<DmarcAggregateEvidence> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(snapshot -> snapshot.organizationId().equals(organizationId))
            .toList();
    }

    @Override
    public List<DmarcAggregateEvidence> findByDomainIdAndReportId(String domainId, String reportId) {
        return byId.values().stream()
            .filter(snapshot -> snapshot.domainId().equals(domainId)
                && snapshot.reportId().equals(reportId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
        occupancy.clear();
    }
}
