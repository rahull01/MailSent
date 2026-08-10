package com.mailsent.portfolio.client.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * ClientRelationshipService — the CLIENT-001 application boundary.
 *
 * <p>Application operations are limited to the approved lifecycle: {@link #form} (an Agency
 * Organization takes on an existing Client Organization), {@link #findById} (read), and
 * {@link #end} (ACTIVE → ENDED, irreversible). There is no generic update, no physical
 * delete, and — per the frozen contract — no HTTP, no authorization (AUTH-006 deferred), no
 * portfolio, and no membership semantics.</p>
 *
 * <p>Both referenced organizations must already exist (CLIENT-001 never creates one); the
 * V7 foreign keys are the final integrity backstop. A {@link Clock} is injected for
 * deterministic lifecycle timestamps, mirroring the organization module.</p>
 */
public class ClientRelationshipService {

    private final ClientRelationshipRepository relationships;
    private final OrganizationRepository organizations;
    private final Clock clock;

    public ClientRelationshipService(ClientRelationshipRepository relationships,
                                     OrganizationRepository organizations, Clock clock) {
        this.relationships = relationships;
        this.organizations = organizations;
        this.clock = clock;
    }

    /**
     * Forms an ACTIVE Agency ↔ Client relationship between two existing organizations.
     *
     * @param agencyOrganizationId the Agency Organization's stable identity
     * @param clientOrganizationId the Client Organization's stable identity
     * @return the persisted ACTIVE relationship
     * @throws OrganizationNotFoundException if either organization does not exist
     * @throws IllegalArgumentException      if an id is missing or both ids are the same
     */
    public ClientRelationship form(String agencyOrganizationId, String clientOrganizationId) {
        organizations.findById(agencyOrganizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(agencyOrganizationId));
        organizations.findById(clientOrganizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(clientOrganizationId));

        ClientRelationship relationship = ClientRelationship.form(
            agencyOrganizationId, clientOrganizationId, Instant.now(clock));
        return relationships.save(relationship);
    }

    /** Reads a relationship by its stable identity; empty when it does not exist. */
    public Optional<ClientRelationship> findById(String id) {
        return relationships.findById(id);
    }

    /**
     * Ends a relationship (ACTIVE → ENDED, irreversible). A no-op if already ended; empty
     * when the relationship does not exist. Either organization's identity or lifecycle is
     * never altered.
     */
    public Optional<ClientRelationship> end(String id) {
        return relationships.findById(id)
            .map(relationship -> {
                relationship.end(Instant.now(clock));
                return relationships.save(relationship);
            });
    }
}