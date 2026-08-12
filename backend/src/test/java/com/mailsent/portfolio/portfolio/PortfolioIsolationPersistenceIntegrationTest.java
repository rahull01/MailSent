package com.mailsent.portfolio.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.application.PortfolioService;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.Portfolio;

/**
 * Real-PostgreSQL integration tests for the PORT-003 INV-09 isolation invariant over the
 * approved Portfolio (PORT-001) and PortfolioOverview (PORT-002) reads (test-only; no
 * production change).
 *
 * <p>Proves Agency A can never receive Portfolio/Client/Domain data belonging to Agency B,
 * mirroring the approved ORG-004 / CLIENT-003 isolation pattern. It adds the broader
 * <strong>multi-agency partition</strong> coverage not already asserted by the PORT-001/002
 * tests (which cover per-agency and two-agency cases):</p>
 *
 * <ul>
 *   <li>multiple agency/client relationships partition the client universe (no-overlap +
 *       totality);</li>
 *   <li>multiple agencies' portfolio overviews partition the domain universe (no-overlap +
 *       totality);</li>
 *   <li>an empty portfolio/overview stays empty and unaffected;</li>
 *   <li>ending a relationship in one agency never leaks clients/domains into another
 *       agency's portfolio or overview.</li>
 * </ul>
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}); Flyway
 * applies V1–V7 at startup. Gated on {@code mailsent.it.postgres}; idempotent (randomized
 * org/domain names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class PortfolioIsolationPersistenceIntegrationTest {

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService clientRelationships;
    @Autowired private DomainService domains;
    @Autowired private PortfolioService portfolios;
    @Autowired private PortfolioOverviewService portfolioOverview;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    @Test
    void portfoliosPartitionTheAgencyClientUniverse() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String agencyC = createdOrganization("agencyC");
        String emptyAgency = createdOrganization("emptyAgency");
        String ca1 = createdOrganization("ca1");
        String ca2 = createdOrganization("ca2");
        String cb1 = createdOrganization("cb1");
        String cc1 = createdOrganization("cc1");
        String cc2 = createdOrganization("cc2");

        // Interleave relationships across the three agencies.
        clientRelationships.form(agencyA, ca1);
        clientRelationships.form(agencyB, cb1);
        clientRelationships.form(agencyA, ca2);
        clientRelationships.form(agencyC, cc1);
        clientRelationships.form(agencyC, cc2);

        Set<String> portfolioA = clientsOf(portfolios.findByAgency(agencyA));
        Set<String> portfolioB = clientsOf(portfolios.findByAgency(agencyB));
        Set<String> portfolioC = clientsOf(portfolios.findByAgency(agencyC));
        Set<String> portfolioEmpty = clientsOf(portfolios.findByAgency(emptyAgency));

        assertEquals(Set.of(ca1, ca2), portfolioA, "portfolio A is exactly agency A's clients");
        assertEquals(Set.of(cb1), portfolioB);
        assertEquals(Set.of(cc1, cc2), portfolioC);
        assertTrue(portfolioEmpty.isEmpty(), "an empty agency's portfolio is unaffected");

        // No-overlap: a client never appears in two agencies' portfolios (INV-09).
        assertNoOverlap(List.of(portfolioA, portfolioB, portfolioC), "client");

        // Totality: every formed client relationship is reachable via exactly one portfolio.
        assertEquals(5, unionSize(portfolioA, portfolioB, portfolioC),
            "all five client organizations are owned by exactly one agency portfolio");
    }

    @Test
    void portfolioOverviewsPartitionTheDomainUniverse() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String ca1 = createdOrganization("ca1");
        String ca2 = createdOrganization("ca2");
        String cb1 = createdOrganization("cb1");
        clientRelationships.form(agencyA, ca1);
        clientRelationships.form(agencyA, ca2);
        clientRelationships.form(agencyB, cb1);

        domains.register(ca1, PREFIX + "a1.com");
        domains.register(ca1, PREFIX + "a1b.com");
        domains.register(ca2, PREFIX + "a2.com");
        domains.register(cb1, PREFIX + "b1.com");

        Set<String> overviewA = domainNamesOf(portfolioOverview.overviewByAgency(agencyA).clients());
        Set<String> overviewB = domainNamesOf(portfolioOverview.overviewByAgency(agencyB).clients());

        assertEquals(Set.of(PREFIX + "a1.com", PREFIX + "a1b.com", PREFIX + "a2.com"), overviewA,
            "overview A is exactly agency A's clients' domains");
        assertEquals(Set.of(PREFIX + "b1.com"), overviewB);

        // No-overlap + totality across the domain universe (INV-09).
        assertNoOverlap(List.of(overviewA, overviewB), "domain");
        assertEquals(4, unionSize(overviewA, overviewB),
            "all four client domains are owned by exactly one agency overview");
    }

    @Test
    void emptySummariesAreUnaffectedByOtherAgencies() {
        String busyAgency = createdOrganization("busyAgency");
        String calmAgency = createdOrganization("calmAgency");
        String clientOfBusy = createdOrganization("clientOfBusy");
        clientRelationships.form(busyAgency, clientOfBusy);
        domains.register(clientOfBusy, PREFIX + "busy.com");

        // The calm agency has no relationships and no domains, and stays empty afterwards.
        assertTrue(clientsOf(portfolios.findByAgency(calmAgency)).isEmpty());
        assertTrue(domainNamesOf(portfolioOverview.overviewByAgency(calmAgency).clients()).isEmpty());
    }

    @Test
    void endingARelationshipDoesNotLeakIntoOtherPortfolios() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String clientA = createdOrganization("clientA");
        String clientB = createdOrganization("clientB");
        ClientRelationship relA = clientRelationships.form(agencyA, clientA);
        clientRelationships.form(agencyB, clientB);
        domains.register(clientA, PREFIX + "a.com");
        domains.register(clientB, PREFIX + "b.com");

        clientRelationships.end(relA.id());

        assertEquals(Set.of(clientB), clientsOf(portfolios.findByAgency(agencyB)),
            "ending agency A's relationship never alters agency B's portfolio");
        assertEquals(Set.of(PREFIX + "b.com"), domainNamesOf(
            portfolioOverview.overviewByAgency(agencyB).clients()),
            "ending agency A's relationship never leaks domains into agency B's overview");
        assertEquals(Set.of(clientA), clientsOf(portfolios.findByAgency(agencyA)),
            "agency A's portfolio still contains its (ended) client — only lifecycle changed");
    }

    private static Set<String> clientsOf(Portfolio portfolio) {
        Set<String> clients = new HashSet<>();
        for (ClientRelationship relationship : portfolio.relationships()) {
            clients.add(relationship.clientOrganizationId());
        }
        return clients;
    }

    private static Set<String> domainNamesOf(List<ClientOverview> clients) {
        Set<String> domains = new HashSet<>();
        for (ClientOverview client : clients) {
            client.domains().forEach(d -> domains.add(d.name()));
        }
        return domains;
    }

    private static int unionSize(Set<String> a, Set<String> b, Set<String> c) {
        Set<String> union = new HashSet<>();
        union.addAll(a);
        union.addAll(b);
        union.addAll(c);
        return union.size();
    }

    private static int unionSize(Set<String> a, Set<String> b) {
        Set<String> union = new HashSet<>();
        union.addAll(a);
        union.addAll(b);
        return union.size();
    }

    private static void assertNoOverlap(List<Set<String>> partitions, String kind) {
        Set<String> seen = new HashSet<>();
        for (Set<String> partition : partitions) {
            for (String id : partition) {
                assertTrue(seen.add(id),
                    kind + " '" + id + "' must not appear under two organizations (INV-09)");
            }
        }
    }
}