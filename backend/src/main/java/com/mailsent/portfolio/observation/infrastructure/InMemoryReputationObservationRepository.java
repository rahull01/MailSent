package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.ReputationObservationRepository;
import com.mailsent.portfolio.observation.domain.ReputationObservation;

/**
 * InMemoryReputationObservationRepository — a hermetic, thread-safe
 * {@link ReputationObservationRepository} for the test profile. Snapshots are
 * keyed by their stable identity; lookups by domain or organization scan the
 * store.
 */
public class InMemoryReputationObservationRepository implements ReputationObservationRepository {

    private final Map<String, ReputationObservation> byId = new ConcurrentHashMap<>();

    @Override
    public ReputationObservation save(ReputationObservation observation) {
        byId.put(observation.id(), observation);
        return observation;
    }

    @Override
    public Optional<ReputationObservation> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<ReputationObservation> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(observation -> observation.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<ReputationObservation> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(observation -> observation.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
