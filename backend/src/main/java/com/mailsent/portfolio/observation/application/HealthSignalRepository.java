package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * HealthSignalRepository — the port for persisting and retrieving Health
 * Signal snapshots (HEALTH-001).
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The
 * durable PostgreSQL adapter is wired in the non-test profile; a hermetic
 * in-memory adapter backs the tests. Insert-only: there is no update or delete.
 * Repeated derivations of the same domain are additional rows.</p>
 */
public interface HealthSignalRepository {

    /** Persists a new snapshot and returns it. */
    HealthSignal save(HealthSignal signal);

    /** Loads a snapshot by its stable identity, if present. */
    Optional<HealthSignal> findById(String id);

    /** Lists all snapshots captured for a single registered domain. */
    List<HealthSignal> findByDomainId(String domainId);

    /** Lists all snapshots belonging to a single organization. */
    List<HealthSignal> findByOrganizationId(String organizationId);
}
