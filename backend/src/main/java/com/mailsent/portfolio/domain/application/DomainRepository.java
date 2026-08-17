package com.mailsent.portfolio.domain.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * DomainRepository — the port for persisting and retrieving sending domains.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The durable
 * PostgreSQL adapter is wired in the non-test profile; a hermetic in-memory adapter backs
 * the tests. The ORG-003 surface (register/lookup-by-identity/lookup-by-name/
 * lookup-by-organization/exists) plus {@link #findAll()} for scheduled re-observation
 * (INF-004) are declared — reassignment and sender resolution are later ORG tasks.</p>
 */
public interface DomainRepository {

    /** Persists a domain and returns it. */
    Domain save(Domain domain);

    /** Loads a domain by its stable identity, if present. */
    Optional<Domain> findById(String id);

    /** Loads a domain by its canonical (case-folded) name, if present. */
    Optional<Domain> findByName(String name);

    /** Lists all domains owned by a single organization. */
    List<Domain> findByOrganizationId(String organizationId);

    /** Lists every registered domain (INF-004 re-observation). */
    List<Domain> findAll();

    /** Whether a canonical name is already registered (used for the duplicate check). */
    boolean existsByName(String name);
}