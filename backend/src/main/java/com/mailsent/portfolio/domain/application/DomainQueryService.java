package com.mailsent.portfolio.domain.application;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.application.AuthorizationService;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.domain.Capability;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * DomainQueryService — Application query service for domain listing and single-domain lookup (DOM-003).
 *
 * <p>Enforces:
 * 1. Multi-Tenant Isolation (INV-09): Queries are strictly scoped to the caller's home organization.
 * 2. Capability Authorization (TEAM-002): Governed by {@link Capability#HEALTH_VIEW} (OWNER, ANALYST, VIEWER).
 * 3. Organization Validation (INV-14): Target organization must exist in OrganizationRepository.
 * 4. Grounded Truth (N2): Exposes only real persisted domain attributes via {@link DomainSummary}.
 * 5. Deterministic Ordering: Ordered by name ASC, id ASC.</p>
 */
public class DomainQueryService {

    private final DomainRepository domainRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthorizationService authorizationService;

    public DomainQueryService(DomainRepository domainRepository,
                              OrganizationRepository organizationRepository,
                              AuthorizationService authorizationService) {
        this.domainRepository = Objects.requireNonNull(domainRepository, "domainRepository must not be null");
        this.organizationRepository = Objects.requireNonNull(organizationRepository, "organizationRepository must not be null");
        this.authorizationService = Objects.requireNonNull(authorizationService, "authorizationService must not be null");
    }

    /**
     * Lists all registered domains for the specified organization.
     *
     * @param caller         the authenticated user requesting the domain list
     * @param organizationId the target organization id
     * @return deterministic list of domain summaries
     * @throws AccessDeniedException               if caller is unauthenticated, tenant mismatch, or lacks HEALTH_VIEW
     * @throws DomainOrganizationNotFoundException if target organization does not exist
     */
    public List<DomainSummary> listDomains(User caller, String organizationId) {
        verifyAuthorizationAndOrgExistence(caller, organizationId);

        return domainRepository.findByOrganizationId(organizationId).stream()
                .sorted(Comparator.comparing(Domain::name).thenComparing(Domain::id))
                .map(this::toSummary)
                .toList();
    }

    /**
     * Looks up a single domain by canonical name within the caller's organization.
     *
     * @param caller         the authenticated user requesting the domain
     * @param organizationId the target organization id
     * @param domainName     the domain name to lookup
     * @return Optional containing DomainSummary if found and owned by organizationId; Optional.empty() otherwise
     * @throws AccessDeniedException               if caller is unauthenticated, tenant mismatch, or lacks HEALTH_VIEW
     * @throws DomainOrganizationNotFoundException if target organization does not exist
     */
    public Optional<DomainSummary> getDomain(User caller, String organizationId, String domainName) {
        verifyAuthorizationAndOrgExistence(caller, organizationId);

        if (domainName == null || domainName.isBlank()) {
            return Optional.empty();
        }

        String normalizedName = Domain.normalize(domainName);
        return domainRepository.findByName(normalizedName)
                .filter(domain -> organizationId.equals(domain.organizationId()))
                .map(this::toSummary);
    }

    private void verifyAuthorizationAndOrgExistence(User caller, String organizationId) {
        if (caller == null) {
            throw new AccessDeniedException("anonymous", organizationId, Capability.HEALTH_VIEW, "Caller is unauthenticated.");
        }

        if (organizationId == null || organizationId.isBlank()) {
            throw new AccessDeniedException(caller.id(), "none", Capability.HEALTH_VIEW, "Organization ID must not be blank.");
        }

        // Evaluate capability authorization & multi-tenant isolation via AuthorizationService
        if (!authorizationService.isAuthorized(caller, organizationId, Capability.HEALTH_VIEW)) {
            throw new AccessDeniedException(caller.id(), organizationId, Capability.HEALTH_VIEW,
                    String.format("Caller '%s' is not authorized for HEALTH_VIEW in organization '%s'",
                            caller.id(), organizationId));
        }

        // Verify organization existence (INV-14)
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new DomainOrganizationNotFoundException(organizationId));
    }

    private DomainSummary toSummary(Domain domain) {
        return new DomainSummary(
                domain.id(),
                domain.organizationId(),
                domain.name(),
                domain.createdAt()
        );
    }
}
