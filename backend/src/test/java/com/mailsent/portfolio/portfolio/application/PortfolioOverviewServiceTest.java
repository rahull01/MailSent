package com.mailsent.portfolio.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.client.infrastructure.InMemoryClientRelationshipRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * Unit tests for {@link PortfolioOverviewService} (PORT-002) against the hermetic in-memory
 * repositories: the overview aggregates each client's owned domains and never crosses an
 * organization boundary (INV-09).
 */
class PortfolioOverviewServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryClientRelationshipRepository relationships;
    private InMemoryDomainRepository domains;
    private PortfolioOverviewService service;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        relationships = new InMemoryClientRelationshipRepository();
        domains = new InMemoryDomainRepository();
        service = new PortfolioOverviewService(relationships, domains, organizations);
    }

    private String createdOrganization(String name) {
        return organizations.save(Organization.create(name, CLOCK.instant())).id();
    }

    private void relationship(String agency, String client) {
        relationships.save(ClientRelationship.form(agency, client, CLOCK.instant()));
    }

    private void domain(String orgId, String name) {
        domains.save(Domain.register(orgId, name, CLOCK.instant()));
    }

    @Test
    void overviewByAgencyAggregatesEachClientsDomains() {
        String agencyA = createdOrganization("Agency A");
        String clientA1 = createdOrganization("Client A1");
        String clientA2 = createdOrganization("Client A2");
        relationship(agencyA, clientA1);
        relationship(agencyA, clientA2);
        domain(clientA1, "a1.com");
        domain(clientA1, "a1b.com");
        domain(clientA2, "a2.com");

        PortfolioOverview overview = service.overviewByAgency(agencyA);

        assertEquals(agencyA, overview.agencyOrganizationId());
        assertEquals(2, overview.clients().size());
        for (ClientOverview c : overview.clients()) {
            assertEquals(clientA1.equals(c.clientOrganizationId()) ? 2 : 1, c.domainCount());
        }
    }

    @Test
    void overviewByAgencyIsIsolatedAcrossAgencies() {
        String agencyA = createdOrganization("Agency A");
        String agencyB = createdOrganization("Agency B");
        String clientA = createdOrganization("Client A");
        String clientB = createdOrganization("Client B");
        relationship(agencyA, clientA);
        relationship(agencyB, clientB);
        domain(clientA, "clienta.com");
        domain(clientB, "clientb.com");

        List<String> domainsInA = service.overviewByAgency(agencyA).clients().stream()
            .flatMap(c -> c.domains().stream()).map(Domain::name).toList();

        assertEquals(List.of("clienta.com"), domainsInA);
        assertTrue(!domainsInA.contains("clientb.com"),
            "a domain of agency B's client never appears in agency A's overview (INV-09)");
    }

    @Test
    void overviewByAgencyForAgencyWithNoClientsReturnsEmptyClients() {
        String agency = createdOrganization("Agency");
        assertTrue(service.overviewByAgency(agency).clients().isEmpty());
    }

    @Test
    void clientWithNoDomainsHasEmptyDomainList() {
        String agency = createdOrganization("Agency");
        String client = createdOrganization("Client");
        relationship(agency, client);

        PortfolioOverview overview = service.overviewByAgency(agency);
        assertEquals(1, overview.clients().size());
        assertEquals(0, overview.clients().get(0).domainCount());
    }

    @Test
    void overviewByAgencyRejectsUnknownAgency() {
        assertThrows(OrganizationNotFoundException.class,
            () -> service.overviewByAgency("no-such-agency"));
    }
}