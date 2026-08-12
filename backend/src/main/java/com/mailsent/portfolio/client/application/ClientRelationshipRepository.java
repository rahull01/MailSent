package com.mailsent.portfolio.client.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * ClientRelationshipRepository — the port for persisting and retrieving client relationships.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. Declares the lifecycle
 * operations supported by CLIENT-001 ({@link #save}, {@link #findById}) plus the agency-scoped
 * read required by PORT-001's Portfolio container ({@link #findByAgencyOrganizationId}); no
 * membership or authorization surface exists.</p>
 */
public interface ClientRelationshipRepository {

    /** Persists a client relationship and returns it. */
    ClientRelationship save(ClientRelationship relationship);

    /** Loads a client relationship by its stable identity, if present. */
    Optional<ClientRelationship> findById(String id);

    /** Lists the relationships of one Agency Organization (its Portfolio container). */
    List<ClientRelationship> findByAgencyOrganizationId(String agencyOrganizationId);
}