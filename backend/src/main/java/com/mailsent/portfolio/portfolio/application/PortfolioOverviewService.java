package com.mailsent.portfolio.portfolio.application;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.DomainOverview;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * PortfolioOverviewService — the PORT-002 application boundary: the overview data of an
 * Agency Organization's Portfolio (FUN-D4).
 *
 * <p>The overview is built exclusively from real persisted data: the agency's
 * {@link ClientRelationship}s (its Portfolio container), per client Organization the
 * domains it owns ({@link DomainRepository#findByOrganizationId}, the ORG-003 registry),
 * and per domain its real HEALTH-001 indication when one has been persisted. The "who is
 * at risk" dimension of FUN-D4 therefore comes from the canonical Health Signal only — it
 * is passed through verbatim, never recalculated and never fabricated (N2/INV-01). A
 * domain without a persisted signal carries a {@code null} indication: an explicit absent
 * state, not an invented one.</p>
 *
 * <p>The overview never crosses an organization boundary (INV-09): clients are read from
 * the agency's own relationships, domains from each client's own registry, and a signal is
 * exposed only when its {@code organizationId} matches the owning client — a foreign-org
 * signal is ignored as if absent.</p>
 */
public class PortfolioOverviewService {

    private final ClientRelationshipRepository clientRelationships;
    private final DomainRepository domains;
    private final OrganizationRepository organizations;
    private final HealthSignalService healthSignals;

    public PortfolioOverviewService(ClientRelationshipRepository clientRelationships,
                                    DomainRepository domains, OrganizationRepository organizations,
                                    HealthSignalService healthSignals) {
        this.clientRelationships = clientRelationships;
        this.domains = domains;
        this.organizations = organizations;
        this.healthSignals = healthSignals;
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
            .map(clientId -> ClientOverview.of(clientId,
                domains.findByOrganizationId(clientId).stream().map(this::toDomainOverview).toList()))
            .toList();

        return PortfolioOverview.of(agencyOrganizationId, clients);
    }

    /**
     * Projects one owned domain with its latest real indication. Latest follows the
     * HEALTH-001 ordering convention (observedAt, then id). Signals owned by another
     * organization are excluded (INV-09 ownership guard); no signal at all yields
     * {@code null} — explicit absence.
     */
    private DomainOverview toDomainOverview(com.mailsent.portfolio.domain.domain.Domain domain) {
        return new DomainOverview(domain.id(), domain.name(), latestIndication(domain));
    }

    private HealthIndication latestIndication(com.mailsent.portfolio.domain.domain.Domain domain) {
        Optional<HealthSignal> latest = healthSignals.findByDomainId(domain.id()).stream()
            .filter(signal -> signal.organizationId().equals(domain.organizationId()))
            .max(Comparator.comparing(HealthSignal::observedAt).thenComparing(HealthSignal::id));
        return latest.map(HealthSignal::indication).orElse(null);
    }
}
