package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.SmtpGreetingObservationRepository;
import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;

/**
 * InMemorySmtpGreetingObservationRepository — a hermetic, thread-safe
 * {@link SmtpGreetingObservationRepository} for the test profile. Snapshots are
 * keyed by their stable identity; lookups by domain or organization scan the
 * store.
 */
public class InMemorySmtpGreetingObservationRepository implements SmtpGreetingObservationRepository {

    private final Map<String, SmtpGreetingObservation> byId = new ConcurrentHashMap<>();

    @Override
    public SmtpGreetingObservation save(SmtpGreetingObservation observation) {
        byId.put(observation.id(), observation);
        return observation;
    }

    @Override
    public Optional<SmtpGreetingObservation> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<SmtpGreetingObservation> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(observation -> observation.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<SmtpGreetingObservation> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(observation -> observation.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
