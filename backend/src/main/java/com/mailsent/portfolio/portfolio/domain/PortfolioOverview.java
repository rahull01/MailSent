package com.mailsent.portfolio.portfolio.domain;

import java.util.List;

/**
 * PortfolioOverview — the PORT-002 overview data of an Agency Organization's Portfolio.
 *
 * <p>A projection over real persisted data: the owning Agency Organization and, for each of
 * its clients (the {@link Portfolio} container), a {@link ClientOverview} of the domains the
 * client owns. The overview never crosses an organization boundary (INV-09): each client
 * belongs to exactly one agency's portfolio and each domain to exactly one client
 * Organization. Delivery health/risk per domain is intentionally absent — it is a dependency
 * on the Domain Health / Observation pipeline (Phases 08–16) and is never fabricated.</p>
 */
public final class PortfolioOverview {

    private final String agencyOrganizationId;
    private final List<ClientOverview> clients;

    private PortfolioOverview(String agencyOrganizationId, List<ClientOverview> clients) {
        this.agencyOrganizationId = agencyOrganizationId;
        this.clients = List.copyOf(clients);
    }

    /**
     * Builds the portfolio overview for an Agency Organization.
     *
     * @param agencyOrganizationId the owning Agency Organization's stable identity, required
     * @param clients              the per-client overviews, required (may be empty)
     * @throws IllegalArgumentException if the agency id is blank or {@code clients} is null
     */
    public static PortfolioOverview of(String agencyOrganizationId, List<ClientOverview> clients) {
        if (agencyOrganizationId == null || agencyOrganizationId.isBlank()) {
            throw new IllegalArgumentException("An agency organization id is required for a portfolio overview.");
        }
        if (clients == null) {
            throw new IllegalArgumentException("A portfolio overview must be built from its clients.");
        }
        return new PortfolioOverview(agencyOrganizationId, clients);
    }

    /** The owning Agency Organization's stable identity. */
    public String agencyOrganizationId() {
        return agencyOrganizationId;
    }

    /** The per-client overviews in this portfolio. */
    public List<ClientOverview> clients() {
        return clients;
    }
}