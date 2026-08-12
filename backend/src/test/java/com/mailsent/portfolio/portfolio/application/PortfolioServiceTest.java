package com.mailsent.portfolio.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.client.infrastructure.InMemoryClientRelationshipRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;
import com.mailsent.portfolio.portfolio.domain.Portfolio;

/**
 * Unit tests for {@link PortfolioService} (PORT-001) against the hermetic in-memory
 * repositories: the Portfolio container of an Agency Organization is a projection over its
 * Client Relationships, never another agency's.
 */
class PortfolioServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryClientRelationshipRepository relationships;
    private PortfolioService portfolioService;
    private ClientRelationshipService clientRelationships;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        relationships = new InMemoryClientRelationshipRepository();
        clientRelationships = new ClientRelationshipService(relationships, organizations, CLOCK);
        portfolioService = new PortfolioService(relationships, organizations);
    }

    private String createdOrganization(String name) {
        return organizations.save(Organization.create(name, CLOCK.instant())).id();
    }

    @Test
    void findByAgencyReturnsOnlyThatAgencysRelationships() {
        String agencyA = createdOrganization("Agency A");
        String agencyB = createdOrganization("Agency B");
        String clientA1 = createdOrganization("Client A1");
        String clientA2 = createdOrganization("Client A2");
        String clientB = createdOrganization("Client B");
        clientRelationships.form(agencyA, clientA1);
        clientRelationships.form(agencyA, clientA2);
        clientRelationships.form(agencyB, clientB);

        Portfolio portfolioA = portfolioService.findByAgency(agencyA);
        Portfolio portfolioB = portfolioService.findByAgency(agencyB);

        assertEquals(Set.of(clientA1, clientA2), new HashSet<>(portfolioA.clientOrganizationIds()),
            "portfolio A is exactly agency A's clients");
        assertEquals(Set.of(clientB), new HashSet<>(portfolioB.clientOrganizationIds()),
            "portfolio B is exactly agency B's clients");
        assertTrue(!portfolioA.clientOrganizationIds().contains(clientB),
            "a client of agency B never appears in agency A's portfolio (INV-09)");
    }

    @Test
    void findByAgencyForAgencyWithNoClientsReturnsEmptyPortfolio() {
        String agency = createdOrganization("Agency");
        Portfolio portfolio = portfolioService.findByAgency(agency);
        assertTrue(portfolio.relationships().isEmpty());
        assertEquals(agency, portfolio.agencyOrganizationId());
    }

    @Test
    void findByAgencyRejectsUnknownAgency() {
        assertThrows(OrganizationNotFoundException.class,
            () -> portfolioService.findByAgency("no-such-agency"));
    }

    @Test
    void eachClientBelongsToExactlyOnePortfolio() {
        String agencyA = createdOrganization("Agency A");
        String agencyB = createdOrganization("Agency B");
        String clientA = createdOrganization("Client A");
        String clientB = createdOrganization("Client B");
        clientRelationships.form(agencyA, clientA);
        clientRelationships.form(agencyB, clientB);

        List<String> portfolioA = portfolioService.findByAgency(agencyA).clientOrganizationIds();
        List<String> portfolioB = portfolioService.findByAgency(agencyB).clientOrganizationIds();

        assertEquals(List.of(clientA), portfolioA);
        assertEquals(List.of(clientB), portfolioB);
        // No-overlap across the two portfolios.
        for (String id : portfolioA) {
            assertTrue(!portfolioB.contains(id));
        }
    }
}