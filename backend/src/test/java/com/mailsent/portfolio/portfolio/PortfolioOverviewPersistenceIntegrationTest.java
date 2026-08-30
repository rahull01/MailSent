package com.mailsent.portfolio.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.observation.application.HealthSignalRepository;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * Real-PostgreSQL integration tests for the PORT-002 portfolio overview data.
 *
 * <p>The overview is a projection over persisted client relationships, domain ownership
 * (ORG-003 registry), and persisted HEALTH-001 signals (V16): each domain row carries the
 * real canonical indication when one exists, an explicit {@code null} otherwise — nothing
 * is fabricated. Runs against a live PostgreSQL (see {@code application-it.yml}); Flyway
 * applies V1–V20 at startup and {@code ddl-auto: validate} verifies the schema. Gated on
 * {@code mailsent.it.postgres}; idempotent (randomized org/domain names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class PortfolioOverviewPersistenceIntegrationTest {

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";
    // Signal/lineage ids must fit VARCHAR(36): a short unique seed keeps them within bounds.
    private static final String SID = "s" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-20T12:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService clientRelationships;
    @Autowired private DomainService domains;
    @Autowired private HealthSignalRepository healthSignals;
    @Autowired private PortfolioOverviewService portfolioOverview;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    private void persistedSignal(String id, String organizationId, String domainId,
                                 HealthIndication indication, Instant observedAt) {
        healthSignals.save(HealthSignal.restore(id, organizationId, domainId,
                PREFIX + "signal.example.com", indication,
                SID + "-oe", List.of(SID + "-ae"), List.of(SID + "-rep"), observedAt));
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
            c.domains().forEach(d -> assertNull(d.healthIndication(),
                "no signal was persisted for these domains: explicit absence, never a fabricated state"));
        }
    }

    @Test
    void persistedHealthSignalIsExposedVerbatimThroughRealPostgres() {
        String agency = createdOrganization("agency");
        String client = createdOrganization("client");
        clientRelationships.form(agency, client);
        String healthyId = domains.register(client, PREFIX + "healthy.com").id();
        String atRiskId = domains.register(client, PREFIX + "atrisk.com").id();
        String unobservedId = domains.register(client, PREFIX + "unobserved.com").id();

        persistedSignal(SID + "-sig-h", client, healthyId, HealthIndication.HEALTHY, OBSERVED_AT);
        persistedSignal(SID + "-sig-r", client, atRiskId, HealthIndication.AT_RISK, OBSERVED_AT);

        PortfolioOverview overview = portfolioOverview.overviewByAgency(agency);
        ClientOverview clientOverview = overview.clients().get(0);

        assertEquals(HealthIndication.HEALTHY, indicationOf(clientOverview, healthyId));
        assertEquals(HealthIndication.AT_RISK, indicationOf(clientOverview, atRiskId),
            "the agency sees who is at risk (FUN-D4)");
        assertNull(indicationOf(clientOverview, unobservedId),
            "a domain with no persisted signal stays explicitly absent");
    }

    @Test
    void latestPersistedSnapshotWinsAcrossAppendedSignals() {
        String agency = createdOrganization("agency");
        String client = createdOrganization("client");
        clientRelationships.form(agency, client);
        String domainId = domains.register(client, PREFIX + "changed.com").id();

        persistedSignal(SID + "-sig-old", client, domainId, HealthIndication.HEALTHY,
            OBSERVED_AT.minusSeconds(3600));
        persistedSignal(SID + "-sig-new", client, domainId, HealthIndication.AT_RISK, OBSERVED_AT);

        ClientOverview clientOverview = portfolioOverview.overviewByAgency(agency).clients().get(0);
        assertEquals(HealthIndication.AT_RISK, indicationOf(clientOverview, domainId),
            "the most recent appended snapshot is shown (HEALTH-001 ordering)");
    }

    @Test
    void foreignOrganizationSignalNeverLeaksIntoTheOverview() {
        String agencyA = createdOrganization("agencyA");
        String clientA = createdOrganization("clientA");
        String strangerOrg = createdOrganization("stranger");
        clientRelationships.form(agencyA, clientA);
        String domainId = domains.register(clientA, PREFIX + "owned.com").id();

        persistedSignal(SID + "-sig-stranger", strangerOrg, domainId, HealthIndication.HEALTHY, OBSERVED_AT);

        ClientOverview clientOverview = portfolioOverview.overviewByAgency(agencyA).clients().get(0);
        assertNull(indicationOf(clientOverview, domainId),
            "a signal owned by another organization is ignored as if absent (INV-09)");
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

    private static HealthIndication indicationOf(ClientOverview clientOverview, String domainId) {
        return clientOverview.domains().stream()
            .filter(d -> d.domainId().equals(domainId))
            .findFirst()
            .orElseThrow()
            .healthIndication();
    }
}
