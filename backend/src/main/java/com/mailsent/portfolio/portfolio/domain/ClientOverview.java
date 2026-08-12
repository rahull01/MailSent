package com.mailsent.portfolio.portfolio.domain;

import java.util.List;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * ClientOverview — one client Organization's overview within a {@link PortfolioOverview}
 * (PORT-002).
 *
 * <p>A projection over real persisted data: the client Organization's stable identity and the
 * domains it owns ({@link Domain}, the ORG-003 registry, scoped by
 * {@code Domain.organizationId}). No health/risk value is fabricated here — the delivery
 * health/risk dimension of FUN-D4 depends on the Domain Health / Observation pipeline
 * (Phases 08–16), which does not yet exist.</p>
 */
public final class ClientOverview {

    private final String clientOrganizationId;
    private final List<Domain> domains;

    private ClientOverview(String clientOrganizationId, List<Domain> domains) {
        this.clientOrganizationId = clientOrganizationId;
        this.domains = List.copyOf(domains);
    }

    /**
     * Builds the overview of a client Organization from its owned domains.
     *
     * @param clientOrganizationId the client Organization's stable identity, required
     * @param domains              the client's owned domains, required (may be empty)
     * @throws IllegalArgumentException if the client id is blank or {@code domains} is null
     */
    public static ClientOverview of(String clientOrganizationId, List<Domain> domains) {
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

    /** The client's owned domains (real ORG-003 registry data). */
    public List<Domain> domains() {
        return domains;
    }

    /** How many domains the client owns. */
    public int domainCount() {
        return domains.size();
    }
}