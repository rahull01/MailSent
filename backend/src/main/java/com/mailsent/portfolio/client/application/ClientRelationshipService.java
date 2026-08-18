package com.mailsent.portfolio.client.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * ClientRelationshipService — the CLIENT-001 / CLIENT-002 application boundary.
 *
 * <p>Application operations:
 * <ul>
 *   <li>{@link #form} — forms a relationship directly between two organizations (internal CLIENT-001).</li>
 *   <li>{@link #assign} — assigns a client organization to an agency on behalf of an authenticated Owner (CLIENT-002).</li>
 *   <li>{@link #findById} — reads relationship by id.</li>
 *   <li>{@link #end} — transitions ACTIVE → ENDED irreversibly.</li>
 * </ul></p>
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

    /**
     * Assigns a client organization to an agency on behalf of an authenticated agency owner (CLIENT-002).
     *
     * <p>Enforces:
     * 1. Multi-Tenant Isolation (INV-09): Caller's home organization must match the agency organization.
     * 2. Least Authority (09_SECURITY_ARCHITECTURE §4): Caller must possess {@link Role#OWNER}.
     * 3. Target Organization Validation (INV-14): Both agency and client must exist in OrganizationRepository.
     * 4. Self-Assignment Guard: Agency cannot assign itself as a client.
     * 5. Idempotency: Returns existing active relationship if one is already active between agency and client.</p>
     *
     * @param caller               the authenticated user making the assignment
     * @param agencyOrganizationId the Agency Organization's stable identity
     * @param clientOrganizationId the Client Organization's stable identity
     * @return the active ClientRelationship
     * @throws AccessDeniedException         if caller is unauthenticated, not an OWNER, or tenant mismatch (INV-09)
     * @throws OrganizationNotFoundException if either organization does not exist (INV-14)
     * @throws IllegalArgumentException      if self-assignment or invalid inputs
     */
    public ClientRelationship assign(User caller, String agencyOrganizationId, String clientOrganizationId) {
        if (caller == null) {
            throw new AccessDeniedException("anonymous", agencyOrganizationId, null, "Caller is unauthenticated.");
        }

        if (agencyOrganizationId == null || agencyOrganizationId.isBlank()) {
            throw new AccessDeniedException(caller.id(), "none", null, "Agency organization ID must not be blank.");
        }

        // Multi-Tenant Isolation (INV-09): Caller must belong to the agency organization
        if (caller.organizationId() == null || !caller.organizationId().equals(agencyOrganizationId)) {
            throw new AccessDeniedException(caller.id(), agencyOrganizationId, null,
                    String.format("Multi-tenant violation: user home org '%s' does not match agency org '%s'",
                            caller.organizationId(), agencyOrganizationId));
        }

        // Role Authorization: Only OWNER can assign clients to the agency
        if (caller.role() != Role.OWNER) {
            String roleName = caller.role() != null ? caller.role().name() : "none";
            throw new AccessDeniedException(caller.id(), agencyOrganizationId, null,
                    String.format("Role %s is not authorized to assign clients to agency", roleName));
        }

        if (clientOrganizationId == null || clientOrganizationId.isBlank()) {
            throw new IllegalArgumentException("A client organization id is required.");
        }

        if (agencyOrganizationId.equals(clientOrganizationId)) {
            throw new IllegalArgumentException("An organization cannot be its own client.");
        }

        organizations.findById(agencyOrganizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(agencyOrganizationId));
        organizations.findById(clientOrganizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(clientOrganizationId));

        // Idempotency: Check if an active relationship already exists
        Optional<ClientRelationship> active = relationships.findActiveByAgencyAndClient(agencyOrganizationId, clientOrganizationId);
        if (active.isPresent()) {
            return active.get();
        }

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