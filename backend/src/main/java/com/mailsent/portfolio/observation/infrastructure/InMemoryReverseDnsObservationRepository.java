package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.ReverseDnsObservationRepository;
import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;

/**
 * InMemoryReverseDnsObservationRepository — a hermetic, thread-safe
 * {@link ReverseDnsObservationRepository} for the test profile. Snapshots are
 * keyed by their stable identity; lookups by domain or organization scan the
 * store.
 */
public class InMemoryReverseDnsObservationRepository implements ReverseDnsObservationRepository {

    private final Map<String, ReverseDnsObservation> byId = new ConcurrentHashMap<>();

    @Override
    public ReverseDnsObservation save(ReverseDnsObservation observation) {
        byId.put(observation.id(), observation);
        return observation;
    }

    @Override
    public Optional<ReverseDnsObservation> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<ReverseDnsObservation> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(observation -> observation.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<ReverseDnsObservation> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(observation -> observation.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
