package com.mailsent.portfolio.domain.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;

/**
 * InMemoryDomainRepository — a hermetic, thread-safe {@link DomainRepository} for the
 * test profile. Domains are keyed by their stable identity, with a secondary name index
 * for the canonical duplicate check.
 */
public class InMemoryDomainRepository implements DomainRepository {

    private final Map<String, Domain> byId = new ConcurrentHashMap<>();
    private final Map<String, Domain> byName = new ConcurrentHashMap<>();

    @Override
    public Domain save(Domain domain) {
        byId.put(domain.id(), domain);
        byName.put(domain.name(), domain);
        return domain;
    }

    @Override
    public Optional<Domain> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Domain> findByName(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    @Override
    public List<Domain> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(domain -> domain.organizationId().equals(organizationId))
            .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return byName.containsKey(name);
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
        byName.clear();
    }
}