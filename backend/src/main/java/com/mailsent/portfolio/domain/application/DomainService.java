package com.mailsent.portfolio.domain.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.domain.Capability;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * DomainService — the ORG-003 / DOM-001 application boundary.
 *
 * <p>Coordinates domain registration, lookups, and owner-scoped queries under an
 * existing organization.
 * <ul>
 *   <li>{@link #register(String, String)} — internal registration directly under an organization.</li>
 *   <li>{@link #register(User, String, String)} — authenticated registration on behalf of an authorized user (DOM-001).</li>
 *   <li>{@link #findByDomain(String)} — case-insensitive lookup.</li>
 *   <li>{@link #findByOrganizationId(String)} — organization-scoped listing (INV-09).</li>
 * </ul></p>
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
     * Registers a domain to an existing organization on behalf of an authenticated caller (DOM-001).
     *
     * <p>Enforces:
     * 1. Authentication: Caller must be non-null.
     * 2. Multi-Tenant Isolation (INV-09): Caller's home organization must match target organizationId.
     * 3. Capability Authorization (TEAM-002): Caller must possess {@link Capability#DOMAIN_MANAGE} (Role.OWNER).
     * 4. Organization Existence (INV-14): Target organization must exist in OrganizationRepository.
     * 5. Global Domain Uniqueness (INV-10): Duplicate domain names across the platform are rejected.</p>
     *
     * @param caller         the authenticated user registering the domain
     * @param organizationId the owning organization's stable identity
     * @param name           the domain name (case-folded for uniqueness)
     * @return the persisted domain
     * @throws AccessDeniedException               if caller is unauthenticated, tenant mismatch (INV-09), or lacks DOMAIN_MANAGE
     * @throws DomainOrganizationNotFoundException if the organization does not exist (INV-14)
     * @throws DomainAlreadyExistsException        if the domain name is already registered (INV-10)
     * @throws IllegalArgumentException            if domain name or organization id is invalid
     */
    public Domain register(User caller, String organizationId, String name) {
        if (caller == null) {
            throw new AccessDeniedException("anonymous", organizationId, Capability.DOMAIN_MANAGE, "Caller is unauthenticated.");
        }

        if (organizationId == null || organizationId.isBlank()) {
            throw new AccessDeniedException(caller.id(), "none", Capability.DOMAIN_MANAGE, "Organization ID must not be blank.");
        }

        // Multi-Tenant Isolation (INV-09): Caller must belong to the target organization
        if (caller.organizationId() == null || !caller.organizationId().equals(organizationId)) {
            throw new AccessDeniedException(caller.id(), organizationId, Capability.DOMAIN_MANAGE,
                    String.format("Multi-tenant violation: user home org '%s' does not match target org '%s'",
                            caller.organizationId(), organizationId));
        }

        // Capability Authorization: Role must possess DOMAIN_MANAGE (Role.OWNER)
        if (caller.role() != Role.OWNER) {
            String roleName = caller.role() != null ? caller.role().name() : "none";
            throw new AccessDeniedException(caller.id(), organizationId, Capability.DOMAIN_MANAGE,
                    String.format("Role %s is not authorized to manage domains (requires DOMAIN_MANAGE)", roleName));
        }

        return register(organizationId, name);
    }

    /**
     * Registers a domain to a single existing organization (internal/unauthenticated application helper).
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