package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;

/**
 * InMemoryObservationEvidenceRepository — a hermetic, thread-safe
 * {@link ObservationEvidenceRepository} for the test profile. Snapshots are keyed by
 * their stable identity; lookups by domain or organization scan the store.
 */
public class InMemoryObservationEvidenceRepository implements ObservationEvidenceRepository {

    private final Map<String, ObservationEvidence> byId = new ConcurrentHashMap<>();

    @Override
    public ObservationEvidence save(ObservationEvidence evidence) {
        byId.put(evidence.id(), evidence);
        return evidence;
    }

    @Override
    public Optional<ObservationEvidence> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<ObservationEvidence> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(evidence -> evidence.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<ObservationEvidence> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(evidence -> evidence.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
