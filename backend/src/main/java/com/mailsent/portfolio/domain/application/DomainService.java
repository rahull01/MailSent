package com.mailsent.portfolio.domain.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * DomainService — the ORG-003 application boundary.
 *
 * <p>Coordinates the smallest approved domain-operation: {@link #register} under an
 * existing organization. The organization's existence is checked here for a clear domain
 * error (ORG-003 decision 3), and duplicate names are rejected for a clear error too; the
 * database {@code UNIQUE}/{@code FOREIGN KEY} constraints remain the race-safe final
 * backstop. A {@link Clock} is injected for a deterministic registration timestamp.</p>
 *
 * <p><strong>Scope.</strong> No principal/current-user tenant authorization (decision 4),
 * no DNS/health/sender-resolution/reassignment (decision 8). Membership and ownership
 * inherited from the bound organization (ORG-002) are not re-implemented here.</p>
 */
public class DomainService {

    private final DomainRepository domains;
    private final OrganizationRepository organizations;
    private final Clock clock;

    public DomainService(DomainRepository domains, OrganizationRepository organizations, Clock clock) {
        this.domains = domains;
        this.organizations = organizations;
        this.clock = clock;
    }

    /**
     * Registers a domain to a single existing organization.
     *
     * @param organizationId the owning organization's stable identity
     * @param name           the domain name (case-folded for uniqueness)
     * @return the persisted domain
     * @throws DomainOrganizationNotFoundException if the organization does not exist
     * @throws DomainAlreadyExistsException        if the domain name is already registered
     */
    public Domain register(String organizationId, String name) {
        organizations.findById(organizationId)
            .orElseThrow(() -> new DomainOrganizationNotFoundException(organizationId));
        Domain domain = Domain.register(organizationId, name, Instant.now(clock));
        if (domains.existsByName(domain.name())) {
            throw new DomainAlreadyExistsException(domain.name());
        }
        return domains.save(domain);
    }

    /** Loads a domain by its canonical name; empty when it is not registered. */
    public Optional<Domain> findByDomain(String name) {
        return domains.findByName(Domain.normalize(name));
    }

    /** Lists all domains owned by a single organization. */
    public List<Domain> findByOrganizationId(String organizationId) {
        return domains.findByOrganizationId(organizationId);
    }
}