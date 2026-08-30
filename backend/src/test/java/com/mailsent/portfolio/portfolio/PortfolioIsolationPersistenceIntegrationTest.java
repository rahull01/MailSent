package com.mailsent.portfolio.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.infrastructure.persistence.ClientRelationshipJpaEntity;
import com.mailsent.portfolio.client.infrastructure.persistence.JpaClientRelationshipRepository;
import com.mailsent.portfolio.client.infrastructure.persistence.JpaClientRelationshipRepositoryAdapter;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryHealthSignalRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReputationObservationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.application.PortfolioService;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.DomainOverview;
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
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = PortfolioIsolationPersistenceIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class PortfolioIsolationPersistenceIntegrationTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
    @EntityScan(basePackageClasses = {
        OrganizationJpaEntity.class,
        ClientRelationshipJpaEntity.class,
        DomainJpaEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        JpaOrganizationRepository.class,
        JpaClientRelationshipRepository.class,
        JpaDomainRepository.class
    })
    @Import({
        JpaOrganizationRepositoryAdapter.class,
        JpaClientRelationshipRepositoryAdapter.class,
        JpaDomainRepositoryAdapter.class
    })
    static class TestConfig {
        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        OrganizationService organizationService(OrganizationRepository repository, Clock clock) {
            return new OrganizationService(repository, clock);
        }

        @Bean
        ClientRelationshipService clientRelationshipService(ClientRelationshipRepository relationships,
                                                            OrganizationRepository organizations, Clock clock) {
            return new ClientRelationshipService(relationships, organizations, clock);
        }

        @Bean
        DomainService domainService(DomainRepository repository, OrganizationRepository organizations, Clock clock) {
            return new DomainService(repository, organizations, clock);
        }

        @Bean
        PortfolioService portfolioService(ClientRelationshipRepository relationships,
                                          OrganizationRepository organizations) {
            return new PortfolioService(relationships, organizations);
        }

        @Bean
        HealthSignalService healthSignalService(DomainRepository domains, Clock clock) {
            // The hermetic in-memory adapters back every observation port; this slice never
            // derives signals, so the overview health stays explicitly absent.
            return new HealthSignalService(new InMemoryHealthSignalRepository(), domains,
                new InMemoryObservationEvidenceRepository(),
                new InMemoryDmarcAggregateEvidenceRepository(),
                new InMemoryReputationObservationRepository(), clock);
        }

        @Bean
        PortfolioOverviewService portfolioOverviewService(ClientRelationshipRepository relationships,
                                                          DomainRepository domains,
                                                          OrganizationRepository organizations,
                                                          HealthSignalService healthSignals) {
            return new PortfolioOverviewService(relationships, domains, organizations, healthSignals);
        }
    }

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

        clientRelationships.form(agencyA, ca1);
        clientRelationships.form(agencyA, ca2);
        clientRelationships.form(agencyB, cb1);
        clientRelationships.form(agencyC, cc1);

        Portfolio pA = portfolios.findByAgency(agencyA);
        Portfolio pB = portfolios.findByAgency(agencyB);
        Portfolio pC = portfolios.findByAgency(agencyC);
        Portfolio pEmpty = portfolios.findByAgency(emptyAgency);

        // No-overlap partition across 3 populated agencies (INV-09):
        assertEquals(Set.of(ca1, ca2), Set.copyOf(pA.clientOrganizationIds()));
        assertEquals(Set.of(cb1), Set.copyOf(pB.clientOrganizationIds()));
        assertEquals(Set.of(cc1), Set.copyOf(pC.clientOrganizationIds()));
        assertTrue(pEmpty.relationships().isEmpty(), "an agency with no clients returns an empty container");

        Set<String> seen = new HashSet<>();
        for (Portfolio p : List.of(pA, pB, pC, pEmpty)) {
            for (String clientId : p.clientOrganizationIds()) {
                assertTrue(seen.add(clientId),
                    "client " + clientId + " appeared in more than one agency's portfolio (INV-09 violation)");
            }
        }
        assertEquals(Set.of(ca1, ca2, cb1, cc1), seen, "union matches the complete client universe (totality)");
    }

    @Test
    void portfolioOverviewsPartitionTheDomainUniverse() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String ca = createdOrganization("ca");
        String cb = createdOrganization("cb");

        clientRelationships.form(agencyA, ca);
        clientRelationships.form(agencyB, cb);

        Domain da1 = domains.register(ca, PREFIX + "da1.example.com");
        Domain da2 = domains.register(ca, PREFIX + "da2.example.com");
        Domain db1 = domains.register(cb, PREFIX + "db1.example.com");

        var overA = portfolioOverview.overviewByAgency(agencyA);
        var overB = portfolioOverview.overviewByAgency(agencyB);

        assertEquals(1, overA.clients().size());
        assertEquals(1, overB.clients().size());

        ClientOverview coA = overA.clients().get(0);
        ClientOverview coB = overB.clients().get(0);
        assertEquals(ca, coA.clientOrganizationId());
        assertEquals(cb, coB.clientOrganizationId());

        // Domain isolation: Agency A sees only ca's domains, Agency B sees only cb's domains (INV-09):
        Set<String> domainsInA = domainNamesOf(coA);
        Set<String> domainsInB = domainNamesOf(coB);
        assertEquals(Set.of(da1.name(), da2.name()), domainsInA);
        assertEquals(Set.of(db1.name()), domainsInB);

        Set<String> allObserved = new HashSet<>(domainsInA);
        allObserved.addAll(domainsInB);
        assertEquals(3, allObserved.size(), "domain sets across agencies are completely disjoint (INV-09)");
    }

    @Test
    void emptySummariesAreUnaffectedByOtherAgencies() {
        String agencyA = createdOrganization("agencyA");
        String emptyAgency = createdOrganization("emptyAgency");
        String ca = createdOrganization("ca");

        clientRelationships.form(agencyA, ca);
        domains.register(ca, PREFIX + "da.example.com");

        var overEmpty = portfolioOverview.overviewByAgency(emptyAgency);
        assertTrue(overEmpty.clients().isEmpty(),
            "empty agency's overview remains empty even with active clients/domains in other agencies");
    }

    @Test
    void endingARelationshipDoesNotLeakIntoOtherPortfolios() {
        String agencyA = createdOrganization("agencyA");
        String agencyB = createdOrganization("agencyB");
        String ca = createdOrganization("ca");
        String cb = createdOrganization("cb");

        ClientRelationship relA = clientRelationships.form(agencyA, ca);
        clientRelationships.form(agencyB, cb);
        Domain da = domains.register(ca, PREFIX + "da-end.example.com");
        Domain db = domains.register(cb, PREFIX + "db-end.example.com");

        clientRelationships.end(relA.id());

        // Ending relA alters only Agency A's portfolio: relA is marked ENDED;
        // Agency B's portfolio and overview remain strictly untouched.
        Portfolio pB = portfolios.findByAgency(agencyB);
        assertEquals(List.of(cb), pB.clientOrganizationIds());
        assertEquals(ClientRelationship.Status.ACTIVE, pB.relationships().get(0).status());

        var overB = portfolioOverview.overviewByAgency(agencyB);
        assertEquals(1, overB.clients().size());
        assertEquals(Set.of(db.name()), domainNamesOf(overB.clients().get(0)));

        // RelA's domain (da) never leaks into agency B's overview:
        assertTrue(!domainNamesOf(overB.clients().get(0)).contains(da.name()),
            "ended relationship's domain must never leak to other agencies (INV-09)");
    }

    private static Set<String> domainNamesOf(ClientOverview overview) {
        return Set.copyOf(overview.domains().stream().map(DomainOverview::name).toList());
    }
}