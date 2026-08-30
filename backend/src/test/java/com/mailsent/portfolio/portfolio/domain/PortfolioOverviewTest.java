package com.mailsent.portfolio.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;

/**
 * Unit tests for the PORT-002 overview projections ({@link DomainOverview},
 * {@link ClientOverview}, {@link PortfolioOverview}).
 */
class PortfolioOverviewTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void clientOverviewExposesClientIdAndDomainProjections() {
        DomainOverview d1 = new DomainOverview("dom-1", "a.com", HealthIndication.HEALTHY);
        DomainOverview d2 = new DomainOverview("dom-2", "b.com", null);

        ClientOverview overview = ClientOverview.of("org-client", List.of(d1, d2));

        assertEquals("org-client", overview.clientOrganizationId());
        assertEquals(2, overview.domainCount());
        assertEquals(List.of("a.com", "b.com"), overview.domains().stream().map(DomainOverview::name).toList());
        assertEquals(HealthIndication.HEALTHY, overview.domains().get(0).healthIndication());
        assertNull(overview.domains().get(1).healthIndication(),
            "absence of a signal is an explicit state, never an invented one");
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
    void domainOverviewRejectsBlankIdentity() {
        assertThrows(IllegalArgumentException.class, () -> new DomainOverview(" ", "a.com", null));
        assertThrows(IllegalArgumentException.class, () -> new DomainOverview("dom-1", " ", null));
        assertThrows(NullPointerException.class, () -> new DomainOverview(null, "a.com", null));
        assertThrows(NullPointerException.class, () -> new DomainOverview("dom-1", null, null));
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
