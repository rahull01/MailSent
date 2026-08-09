package com.mailsent.portfolio.organization.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.organization.domain.Organization;

/**
 * OrganizationService — the ORG-001 application boundary.
 *
 * <p>Coordinates the smallest approved organization operations: create, read, list, and
 * close. It is task-scoped — membership (ORG-002), domain ownership (ORG-003), and
 * cross-organization authorization are not implemented here. A {@link Clock} is injected
 * for deterministic lifecycle timestamps.</p>
 */
public class OrganizationService {

    private final OrganizationRepository organizations;
    private final Clock clock;

    public OrganizationService(OrganizationRepository organizations, Clock clock) {
        this.organizations = organizations;
        this.clock = clock;
    }

    /** Creates and persists a new ACTIVE organization. */
    public Organization create(String name) {
        Organization org = Organization.create(name, Instant.now(clock));
        return organizations.save(org);
    }

    /** Reads an organization by identity; empty when it does not exist. */
    public Optional<Organization> findById(String id) {
        return organizations.findById(id);
    }

    /** Lists all organizations. */
    public List<Organization> findAll() {
        return organizations.findAll();
    }

    /** Closes an organization (lifecycle end). No-op if already closed. */
    public Optional<Organization> close(String id) {
        return organizations.findById(id)
            .map(org -> {
                org.close(Instant.now(clock));
                return organizations.save(org);
            });
    }
}