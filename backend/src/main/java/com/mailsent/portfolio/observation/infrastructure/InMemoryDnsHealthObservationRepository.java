package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.DnsHealthObservationRepository;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;

/**
 * InMemoryDnsHealthObservationRepository — a hermetic, thread-safe
 * {@link DnsHealthObservationRepository} for the test profile. Snapshots are
 * keyed by their stable identity; lookups by domain or organization scan the
 * store.
 */
public class InMemoryDnsHealthObservationRepository implements DnsHealthObservationRepository {

    private final Map<String, DnsHealthObservation> byId = new ConcurrentHashMap<>();

    @Override
    public DnsHealthObservation save(DnsHealthObservation observation) {
        byId.put(observation.id(), observation);
        return observation;
    }

    @Override
    public Optional<DnsHealthObservation> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<DnsHealthObservation> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(observation -> observation.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<DnsHealthObservation> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(observation -> observation.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
