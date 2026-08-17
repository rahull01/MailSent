package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.BlacklistObservationRepository;
import com.mailsent.portfolio.observation.domain.BlacklistObservation;

/**
 * InMemoryBlacklistObservationRepository — a hermetic, thread-safe
 * {@link BlacklistObservationRepository} for the test profile. Snapshots are
 * keyed by their stable identity; lookups by domain or organization scan the
 * store.
 */
public class InMemoryBlacklistObservationRepository implements BlacklistObservationRepository {

    private final Map<String, BlacklistObservation> byId = new ConcurrentHashMap<>();

    @Override
    public BlacklistObservation save(BlacklistObservation observation) {
        byId.put(observation.id(), observation);
        return observation;
    }

    @Override
    public Optional<BlacklistObservation> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<BlacklistObservation> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(observation -> observation.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<BlacklistObservation> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(observation -> observation.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
