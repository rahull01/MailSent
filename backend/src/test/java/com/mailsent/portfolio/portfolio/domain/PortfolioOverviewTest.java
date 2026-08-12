package com.mailsent.portfolio.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * Unit tests for the PORT-002 overview projections ({@link ClientOverview},
 * {@link PortfolioOverview}).
 */
class PortfolioOverviewTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void clientOverviewExposesClientIdAndDomains() {
        Domain d1 = Domain.register("org-client", "a.com", NOW);
        Domain d2 = Domain.register("org-client", "b.com", NOW);

        ClientOverview overview = ClientOverview.of("org-client", List.of(d1, d2));

        assertEquals("org-client", overview.clientOrganizationId());
        assertEquals(2, overview.domainCount());
        assertEquals(List.of("a.com", "b.com"), overview.domains().stream().map(Domain::name).toList());
    }

    @Test
    void clientOverviewRejectsBlankClient() {
        assertThrows(IllegalArgumentException.class, () -> ClientOverview.of("  ", List.of()));
    }

    @Test
    void clientOverviewRejectsNullDomains() {
        assertThrows(IllegalArgumentException.class, () -> ClientOverview.of("org-client", null));
    }

    @Test
    void portfolioOverviewExposesAgencyAndClients() {
        ClientOverview c = ClientOverview.of("org-client", List.of());
        PortfolioOverview overview = PortfolioOverview.of("org-agency", List.of(c));

        assertEquals("org-agency", overview.agencyOrganizationId());
        assertEquals(1, overview.clients().size());
    }

    @Test
    void portfolioOverviewRejectsBlankAgency() {
        assertThrows(IllegalArgumentException.class, () -> PortfolioOverview.of("  ", List.of()));
    }

    @Test
    void portfolioOverviewRejectsNullClients() {
        assertThrows(IllegalArgumentException.class, () -> PortfolioOverview.of("org-agency", null));
    }
}