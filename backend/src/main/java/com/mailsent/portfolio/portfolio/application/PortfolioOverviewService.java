package com.mailsent.portfolio.portfolio.application;

import java.util.List;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * PortfolioOverviewService — the PORT-002 application boundary: the overview data of an
 * Agency Organization's Portfolio.
 *
 * <p>The overview is built exclusively from real persisted data: the agency's
 * {@link ClientRelationship}s (its Portfolio container) and, per client Organization, the
 * domains it owns ({@link DomainRepository#findByOrganizationId}, the ORG-003 registry). It
 * never crosses an organization boundary (INV-09). Delivery health/risk — the "who is at
 * risk" dimension of FUN-D4 — is NOT produced here because no observation/health-signal data
 * exists (Phases 08–16); that remains a genuine downstream dependency and is never fabricated.</p>
 */
public class PortfolioOverviewService {

    private final ClientRelationshipRepository clientRelationships;
    private final DomainRepository domains;
    private final OrganizationRepository organizations;

    public PortfolioOverviewService(ClientRelationshipRepository clientRelationships,
                                    DomainRepository domains, OrganizationRepository organizations) {
        this.clientRelationships = clientRelationships;
        this.domains = domains;
        this.organizations = organizations;
    }

    /**
     * The overview data of an Agency Organization's portfolio.
     *
     * @param agencyOrganizationId the owning Agency Organization's stable identity
     * @return the portfolio overview (empty clients when the agency has none)
     * @throws OrganizationNotFoundException if the agency Organization does not exist
     */
    public PortfolioOverview overviewByAgency(String agencyOrganizationId) {
        organizations.findById(agencyOrganizationId)
            .orElseThrow(() -> new OrganizationNotFoundException(agencyOrganizationId));

        List<ClientRelationship> relationships =
            clientRelationships.findByAgencyOrganizationId(agencyOrganizationId);

        List<ClientOverview> clients = relationships.stream()
            .map(ClientRelationship::clientOrganizationId)
            .distinct()
            .map(clientId -> ClientOverview.of(clientId, domains.findByOrganizationId(clientId)))
            .toList();

        return PortfolioOverview.of(agencyOrganizationId, clients);
    }
}