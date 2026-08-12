package com.mailsent.portfolio.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * Real-PostgreSQL integration tests for the PORT-002 portfolio overview data.
 *
 * <p>The overview is a projection over persisted client relationships and domain ownership
 * (ORG-003 registry) — no health/risk data is fabricated. Runs against a live PostgreSQL on
 * port 5433; Flyway applies V1–V7 at startup and {@code ddl-auto: validate} verifies the
 * schema. Gated on {@code mailsent.it.postgres}; idempotent (randomized org/domain names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class PortfolioOverviewPersistenceIntegrationTest {

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService clientRelationships;
    @Autowired private DomainService domains;
    @Autowired private PortfolioOverviewService portfolioOverview;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    @Test
    void agencyOverviewAggregatesPersistedClientsAndTheirDomains() {
        String agency = createdOrganization("agency");
        String clientA1 = createdOrganization("clientA1");
        String clientA2 = createdOrganization("clientA2");
        clientRelationships.form(agency, clientA1);
        clientRelationships.form(agency, clientA2);
        domains.register(clientA1, PREFIX + "a1.com");
        domains.register(clientA1, PREFIX + "a1b.com");
        domains.register(clientA2, PREFIX + "a2.com");

        PortfolioOverview overview = portfolioOverview.overviewByAgency(agency);

        assertEquals(2, overview.clients().size());
        for (ClientOverview c : overview.clients()) {
            assertEquals(clientA1.equals(c.clientOrganizationId()) ? 2 : 1, c.domainCount());
        }
    }

    @Test
    void overviewIsIsolatedAcrossAgencies() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String clientA = createdOrganization("clientA");
        String clientB = createdOrganization("clientB");
        clientRelationships.form(agencyA, clientA);
        clientRelationships.form(agencyB, clientB);
        domains.register(clientA, PREFIX + "clienta.com");
        domains.register(clientB, PREFIX + "clientb.com");

        Set<String> domainsInA = new HashSet<>();
        portfolioOverview.overviewByAgency(agencyA).clients()
            .forEach(c -> c.domains().forEach(d -> domainsInA.add(d.name())));

        assertTrue(domainsInA.contains(PREFIX + "clienta.com"));
        assertTrue(!domainsInA.contains(PREFIX + "clientb.com"),
            "agency A's overview never contains agency B's client domain (INV-09)");
    }

    @Test
    void agencyWithNoClientsHasEmptyOverview() {
        String agency = createdOrganization("agency");
        assertTrue(portfolioOverview.overviewByAgency(agency).clients().isEmpty());
    }
}