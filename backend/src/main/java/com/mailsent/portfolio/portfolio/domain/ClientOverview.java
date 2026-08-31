package com.mailsent.portfolio.portfolio.domain;

import java.util.List;

/**
 * ClientOverview — one client Organization's overview within a {@link PortfolioOverview}
 * (PORT-002).
 *
 * <p>A projection over real persisted data: the client Organization's stable identity and
 * the domains it owns (the ORG-003 registry), each carrying its real HEALTH-001 indication
 * when one has been persisted ({@link DomainOverview}). Nothing is fabricated: a domain
 * without a Health Signal carries a {@code null} indication, never an invented state.</p>
 */
public final class ClientOverview {

    private final String clientOrganizationId;
    private final List<DomainOverview> domains;

    private ClientOverview(String clientOrganizationId, List<DomainOverview> domains) {
        this.clientOrganizationId = clientOrganizationId;
        this.domains = List.copyOf(domains);
    }

    /**
     * Builds the overview of a client Organization from its owned domains.
     *
     * @param clientOrganizationId the client Organization's stable identity, required
     * @param domains              the client's owned domain projections, required (may be empty)
     * @throws IllegalArgumentException if the client id is blank or {@code domains} is null
     */
    public static ClientOverview of(String clientOrganizationId, List<DomainOverview> domains) {
        if (clientOrganizationId == null || clientOrganizationId.isBlank()) {
            throw new IllegalArgumentException("A client organization id is required.");
        }
        if (domains == null) {
            throw new IllegalArgumentException("A client overview must be built from its domains.");
        }
        return new ClientOverview(clientOrganizationId, domains);
    }

    /** The client Organization's stable identity. */
    public String clientOrganizationId() {
        return clientOrganizationId;
    }

    /** The client's owned domains with their real (or explicitly absent) health. */
    public List<DomainOverview> domains() {
        return domains;
    }

    /** How many domains the client owns. */
    public int domainCount() {
        return domains.size();
    }
}
