package com.mailsent.portfolio.client.application;

import java.util.Optional;

import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * ClientRelationshipRepository — the port for persisting and retrieving client relationships.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. Only the two lifecycle
 * operations supported by CLIENT-001 are declared ({@link #save} and {@link #findById}); no
 * membership, portfolio, or authorization surface exists.</p>
 */
public interface ClientRelationshipRepository {

    /** Persists a client relationship and returns it. */
    ClientRelationship save(ClientRelationship relationship);

    /** Loads a client relationship by its stable identity, if present. */
    Optional<ClientRelationship> findById(String id);
}