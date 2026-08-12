package com.mailsent.portfolio.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.portfolio.application.PortfolioService;
import com.mailsent.portfolio.portfolio.domain.Portfolio;

/**
 * Real-PostgreSQL integration tests for the PORT-001 Portfolio container.
 *
 * <p>Portfolio is a projection over the persisted client_relationships table (no Portfolio
 * catalog). Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml});
 * Flyway applies V1–V7 at startup and {@code ddl-auto: validate} verifies the schema. Gated
 * on {@code mailsent.it.postgres}; idempotent (randomized org names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class PortfolioPersistenceIntegrationTest {

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService clientRelationships;
    @Autowired private PortfolioService portfolioService;
    @Autowired private DataSource dataSource;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    @Test
    void findPortfolioByAgencyReturnsOnlyThatAgencysClients() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String clientA1 = createdOrganization("clientA1");
        String clientA2 = createdOrganization("clientA2");
        String clientB = createdOrganization("clientB");
        clientRelationships.form(agencyA, clientA1);
        clientRelationships.form(agencyA, clientA2);
        clientRelationships.form(agencyB, clientB);

        Portfolio portfolioA = portfolioService.findByAgency(agencyA);
        Portfolio portfolioB = portfolioService.findByAgency(agencyB);

        assertEquals(Set.of(clientA1, clientA2), new HashSet<>(portfolioA.clientOrganizationIds()),
            "portfolio A is exactly agency A's persisted clients");
        assertEquals(Set.of(clientB), new HashSet<>(portfolioB.clientOrganizationIds()));
        assertTrue(!portfolioA.clientOrganizationIds().contains(clientB),
            "a client of agency B never appears in agency A's portfolio (INV-09)");
    }

    @Test
    void findPortfolioByAgencyForAgencyWithNoClientsReturnsEmptyPortfolio() {
        String agency = createdOrganization("agency");
        Portfolio portfolio = portfolioService.findByAgency(agency);
        assertTrue(portfolio.relationships().isEmpty());
        assertEquals(agency, portfolio.agencyOrganizationId());
    }

    @Test
    void portfolioIsAProjectionOverClientRelationships() throws Exception {
        // There is no persisted Portfolio table: the container is derived from the agency's
        // client relationships.
        try (Connection con = DataSourceUtils.getConnection(dataSource);
             ResultSet rs = con.createStatement().executeQuery(
                 "SELECT table_name FROM information_schema.tables WHERE table_name IN ('portfolio','portfolios')")) {
            assertTrue(!rs.next(), "no portfolio table exists — Portfolio is a projection");
        }
    }

    @Test
    void clientBelongsToExactlyOneAgencysPortfolio() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String clientA = createdOrganization("clientA");
        String clientB = createdOrganization("clientB");
        clientRelationships.form(agencyA, clientA);
        clientRelationships.form(agencyB, clientB);

        List<String> portfolioA = portfolioService.findByAgency(agencyA).clientOrganizationIds();
        List<String> portfolioB = portfolioService.findByAgency(agencyB).clientOrganizationIds();

        assertEquals(List.of(clientA), portfolioA);
        assertEquals(List.of(clientB), portfolioB);
        for (String id : portfolioA) {
            assertTrue(!portfolioB.contains(id), "no-overlap across portfolios (client in exactly one)");
        }
    }
}