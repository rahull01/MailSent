package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.BlacklistObservation;

/**
 * BlacklistObservationRepository — the port for persisting and retrieving
 * blacklist observations (INF-008).
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The
 * durable PostgreSQL adapter is wired in the non-test profile; a hermetic
 * in-memory adapter backs the tests. Insert-only: there is no update or delete.
 * Repeated observations of the same domain are additional rows.</p>
 */
public interface BlacklistObservationRepository {

    /** Persists a new snapshot and returns it. */
    BlacklistObservation save(BlacklistObservation observation);

    /** Loads a snapshot by its stable identity, if present. */
    Optional<BlacklistObservation> findById(String id);

    /** Lists all snapshots captured for a single registered domain. */
    List<BlacklistObservation> findByDomainId(String domainId);

    /** Lists all snapshots belonging to a single organization. */
    List<BlacklistObservation> findByOrganizationId(String organizationId);
}
