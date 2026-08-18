package com.mailsent.portfolio.client.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * InMemoryClientRelationshipRepository — a hermetic, thread-safe
 * {@link ClientRelationshipRepository} for the test profile. Relationships are keyed by
 * their stable identity.
 */
public class InMemoryClientRelationshipRepository implements ClientRelationshipRepository {

    private final Map<String, ClientRelationship> byId = new ConcurrentHashMap<>();

    @Override
    public ClientRelationship save(ClientRelationship relationship) {
        byId.put(relationship.id(), relationship);
        return relationship;
    }

    @Override
    public Optional<ClientRelationship> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<ClientRelationship> findByAgencyOrganizationId(String agencyOrganizationId) {
        return byId.values().stream()
            .filter(relationship -> relationship.agencyOrganizationId().equals(agencyOrganizationId))
            .toList();
    }

    @Override
    public Optional<ClientRelationship> findActiveByAgencyAndClient(String agencyOrganizationId, String clientOrganizationId) {
        return byId.values().stream()
            .filter(r -> r.agencyOrganizationId().equals(agencyOrganizationId)
                    && r.clientOrganizationId().equals(clientOrganizationId)
                    && r.status() == ClientRelationship.Status.ACTIVE)
            .findFirst();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}