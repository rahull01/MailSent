package com.mailsent.portfolio.portfolio.application;

import java.util.List;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.portfolio.domain.Portfolio;

/**
 * PortfolioService — the PORT-001 application boundary.
 *
 * <p>Exposes the Agency Organization's {@link Portfolio} container as a projection over its
 * existing {@link ClientRelationship}s ({@code ClientRelationshipRepository
 * .findByAgencyOrganizationId}). No HTTP, no authorization (AUTH-006 deferred), no
 * persistence of a Portfolio, and no portfolio risk/health view (that is PORT-002).</p>
 *
 * <p>The owning Agency Organization must already exist (PORT-001 never creates one); a clear
 * {@link OrganizationNotFoundException} is raised for an unknown agency.</p>
 */
public class PortfolioService {

    private final ClientRelationshipRepository clientRelationships;
    private final OrganizationRepository organizations;

    public PortfolioService(ClientRelationshipRepository clientRelationships, OrganizationRepository organizations) {
        this.clientRelationships = clientRelationships;
        this.organizations = organizations;
    }

    /**
     * The Portfolio container of an Agency Organization — the set of its Client Relationships.
     *
     * @param agencyOrganizationId the owning Agency Organization's stable identity
     * @return the agency's Portfolio (possibly empty when it has no clients yet)
     * @throws OrganizationNotFoundException if the agency Organization does not exist
     */
    public Portfolio findByAgency(String agencyOrganizationId) {
        organizations.findById(agencyOrganizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(agencyOrganizationId));
        List<ClientRelationship> relationships =
            clientRelationships.findByAgencyOrganizationId(agencyOrganizationId);
        return Portfolio.of(agencyOrganizationId, relationships);
    }
}