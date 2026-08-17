package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.HealthSignalRepository;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * InMemoryHealthSignalRepository — a hermetic, thread-safe
 * {@link HealthSignalRepository} for the test profile. Snapshots are keyed by
 * their stable identity; lookups by domain or organization scan the store.
 */
public class InMemoryHealthSignalRepository implements HealthSignalRepository {

    private final Map<String, HealthSignal> byId = new ConcurrentHashMap<>();

    @Override
    public HealthSignal save(HealthSignal signal) {
        byId.put(signal.id(), signal);
        return signal;
    }

    @Override
    public Optional<HealthSignal> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<HealthSignal> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(signal -> signal.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<HealthSignal> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(signal -> signal.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
