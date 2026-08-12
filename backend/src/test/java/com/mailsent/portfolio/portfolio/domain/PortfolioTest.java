package com.mailsent.portfolio.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * Unit tests for the {@link Portfolio} container (PORT-001) — the projection invariants:
 * it belongs to one Agency Organization and never contains another agency's relationship.
 */
class PortfolioTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String AGENCY_A = "org-agency-a";
    private static final String AGENCY_B = "org-agency-b";

    private ClientRelationship relationship(String agency, String client) {
        return ClientRelationship.form(agency, client, NOW);
    }

    @Test
    void ofBuildsAContainerOfThatAgencysRelationships() {
        ClientRelationship r1 = relationship(AGENCY_A, "org-client-a1");
        ClientRelationship r2 = relationship(AGENCY_A, "org-client-a2");

        Portfolio portfolio = Portfolio.of(AGENCY_A, List.of(r1, r2));

        assertEquals(AGENCY_A, portfolio.agencyOrganizationId());
        assertEquals(2, portfolio.relationships().size());
        assertEquals(List.of("org-client-a1", "org-client-a2"), portfolio.clientOrganizationIds());
    }

    @Test
    void ofRejectsBlankAgency() {
        assertThrows(IllegalArgumentException.class, () -> Portfolio.of("  ", List.of()));
    }

    @Test
    void ofRejectsNullRelationships() {
        assertThrows(IllegalArgumentException.class, () -> Portfolio.of(AGENCY_A, null));
    }

    @Test
    void ofRejectsRelationshipNotOwnedByAgency() {
        ClientRelationship foreign = relationship(AGENCY_B, "org-client-b");
        assertThrows(IllegalArgumentException.class, () -> Portfolio.of(AGENCY_A, List.of(foreign)),
            "a portfolio never contains another agency's relationship (INV-09)");
    }

    @Test
    void emptyPortfolioIsAllowed() {
        Portfolio portfolio = Portfolio.of(AGENCY_A, List.of());
        assertTrue(portfolio.relationships().isEmpty());
        assertTrue(portfolio.clientOrganizationIds().isEmpty());
    }

    @Test
    void clientOrganizationIdsAreDistinct() {
        Portfolio portfolio = Portfolio.of(AGENCY_A,
            List.of(relationship(AGENCY_A, "org-client-a1"), relationship(AGENCY_A, "org-client-a1")));
        assertEquals(List.of("org-client-a1"), portfolio.clientOrganizationIds());
    }
}