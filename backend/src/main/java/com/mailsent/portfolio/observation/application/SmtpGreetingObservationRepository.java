package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;

/**
 * SmtpGreetingObservationRepository — the port for persisting and retrieving
 * SMTP greeting observations (INF-006).
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The
 * durable PostgreSQL adapter is wired in the non-test profile; a hermetic
 * in-memory adapter backs the tests. Insert-only: there is no update or delete.
 * Repeated observations of the same domain are additional rows.</p>
 */
public interface SmtpGreetingObservationRepository {

    /** Persists a new snapshot and returns it. */
    SmtpGreetingObservation save(SmtpGreetingObservation observation);

    /** Loads a snapshot by its stable identity, if present. */
    Optional<SmtpGreetingObservation> findById(String id);

    /** Lists all snapshots captured for a single registered domain. */
    List<SmtpGreetingObservation> findByDomainId(String domainId);

    /** Lists all snapshots belonging to a single organization. */
    List<SmtpGreetingObservation> findByOrganizationId(String organizationId);
}
