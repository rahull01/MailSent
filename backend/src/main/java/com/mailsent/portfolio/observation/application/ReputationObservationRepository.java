package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.ReputationObservation;

/**
 * ReputationObservationRepository — the port for persisting and retrieving
 * reputation observations (INF-003).
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The
 * durable PostgreSQL adapter is wired in the non-test profile; a hermetic
 * in-memory adapter backs the tests. Insert-only: there is no update or delete.
 * Repeated observations of the same domain are additional rows.</p>
 */
public interface ReputationObservationRepository {

    /** Persists a new snapshot and returns it. */
    ReputationObservation save(ReputationObservation observation);

    /** Loads a snapshot by its stable identity, if present. */
    Optional<ReputationObservation> findById(String id);

    /** Lists all snapshots captured for a single registered domain. */
    List<ReputationObservation> findByDomainId(String domainId);

    /** Lists all snapshots belonging to a single organization. */
    List<ReputationObservation> findByOrganizationId(String organizationId);
}
