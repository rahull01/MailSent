package com.mailsent.portfolio.organization.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * InMemoryOrganizationRepository — a hermetic, thread-safe {@link OrganizationRepository}
 * for the test profile. Organizations are keyed by their stable identity.</p>
 */
public class InMemoryOrganizationRepository implements OrganizationRepository {

    private final Map<String, Organization> byId = new ConcurrentHashMap<>();

    @Override
    public Organization save(Organization organization) {
        byId.put(organization.id(), organization);
        return organization;
    }

    @Override
    public Optional<Organization> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Organization> findAll() {
        return new ArrayList<>(byId.values());
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}