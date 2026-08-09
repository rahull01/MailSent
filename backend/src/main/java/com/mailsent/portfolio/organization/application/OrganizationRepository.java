package com.mailsent.portfolio.organization.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.organization.domain.Organization;

/**
 * OrganizationRepository — the port for persisting and retrieving organizations.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The durable
 * PostgreSQL adapter is wired in the non-test profile; a hermetic in-memory adapter backs
 * the tests. Only the ORG-001 surface (create/read/list/close) is declared — membership,
 * domain ownership, and cross-organization queries are later ORG tasks.</p>
 */
public interface OrganizationRepository {

    /** Persists an organization and returns it. */
    Organization save(Organization organization);

    /** Loads an organization by its stable identity, if present. */
    Optional<Organization> findById(String id);

    /** Lists all organizations (read surface for ORG-001). */
    List<Organization> findAll();
}