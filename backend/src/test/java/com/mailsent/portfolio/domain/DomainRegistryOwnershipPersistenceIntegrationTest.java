package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.infrastructure.persistence.ClientRelationshipJpaEntity;
import com.mailsent.portfolio.client.infrastructure.persistence.JpaClientRelationshipRepository;
import com.mailsent.portfolio.client.infrastructure.persistence.JpaClientRelationshipRepositoryAdapter;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryHealthSignalRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReputationObservationRepository;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.application.PortfolioService;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.DomainOverview;
import com.mailsent.portfolio.portfolio.domain.Portfolio;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * Real-PostgreSQL integration tests for DOM-004 (Registry ownership and portfolio boundary invariants)
 * over frozen V3, V4, and V5 schemas.
 *
 * <p>Proves against live PostgreSQL:
 * <ul>
 *   <li>Database foreign key constraint (fk_domains_organization) prevents orphan domains.</li>
 *   <li>Database unique constraint (uk_domains_name) prevents duplicate domain registration across organizations.</li>
 *   <li>Domain ownership is strictly persisted to its owning organization.</li>
 *   <li>Portfolio aggregation reads client domains without mutating or transferring ownership in PostgreSQL.</li>
 *   <li>Agency A cannot receive Agency B's client domains.</li>
 * </ul></p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = DomainRegistryOwnershipPersistenceIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DomainRegistryOwnershipPersistenceIntegrationTest {

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
        ClientRelationshipService clientRelationshipService(ClientRelationshipRepository repository,
                                                            OrganizationRepository organizations,
                                                            Clock clock) {
            return new ClientRelationshipService(repository, organizations, clock);
        }

        @Bean
        DomainService domainService(DomainRepository repository, OrganizationRepository organizations, Clock clock) {
            return new DomainService(repository, organizations, clock);
        }

        @Bean
        PortfolioService portfolioService(ClientRelationshipRepository clientRelationships,
                                          OrganizationRepository organizations) {
            return new PortfolioService(clientRelationships, organizations);
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
        PortfolioOverviewService portfolioOverviewService(ClientRelationshipRepository clientRelationships,
                                                          DomainRepository domains,
                                                          OrganizationRepository organizations,
                                                          HealthSignalService healthSignals) {
            return new PortfolioOverviewService(clientRelationships, domains, organizations, healthSignals);
        }
    }

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService relationshipService;
    @Autowired private DomainService domainService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private PortfolioOverviewService overviewService;
    @Autowired private DomainRepository domainRepository;
    @Autowired private JpaDomainRepository jpaDomainRepository;

    private String createOrg(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    private User userWithRole(String orgId, Role role) {
        return User.restore(UUID.randomUUID().toString(), new Email("user@" + PREFIX + "test.com"),
                "hash", role, Instant.now(), orgId);
    }

    @Test
    void foreignKeyConstraintPreventsOrphanDomainInPostgres() {
        String nonExistentOrgId = "org-nonexistent-" + UUID.randomUUID();
        DomainJpaEntity orphanEntity = new DomainJpaEntity(
                UUID.randomUUID().toString(),
                nonExistentOrgId,
                PREFIX + "orphan.example.com",
                Instant.now()
        );

        // Attempting direct DB insert with invalid FK violates fk_domains_organization
        assertThrows(DataIntegrityViolationException.class,
                () -> jpaDomainRepository.saveAndFlush(orphanEntity),
                "Database must reject orphan domains via foreign key (INV-14)");
    }

    @Test
    void uniqueConstraintPreventsDuplicateHostnameAcrossOrganizationsInPostgres() {
        String orgA = createOrg("ownerA");
        String orgB = createOrg("ownerB");
        User ownerA = userWithRole(orgA, Role.OWNER);
        User ownerB = userWithRole(orgB, Role.OWNER);

        String domainName = PREFIX + "unique-domain.example.com";
        domainService.register(ownerA, orgA, domainName);

        // Second registration attempt for Org B is rejected (INV-10):
        assertThrows(DomainAlreadyExistsException.class,
                () -> domainService.register(ownerB, orgB, domainName));
    }

    @Test
    void portfolioAggregationPreservesClientDomainOwnershipInPostgres() {
        String agencyOrg = createOrg("realAgency");
        String clientOrg = createOrg("realClient");
        User agencyOwner = userWithRole(agencyOrg, Role.OWNER);

        relationshipService.assign(agencyOwner, agencyOrg, clientOrg);

        String d1 = PREFIX + "c1.example.com";
        String d2 = PREFIX + "c2.example.com";
        Domain registered1 = domainService.register(clientOrg, d1);
        Domain registered2 = domainService.register(clientOrg, d2);

        // Read portfolio through PortfolioService (PORT-001):
        Portfolio portfolio = portfolioService.findByAgency(agencyOrg);
        assertEquals(1, portfolio.relationships().size());
        assertEquals(clientOrg, portfolio.relationships().get(0).clientOrganizationId());

        // Read portfolio overview through PortfolioOverviewService (PORT-002):
        PortfolioOverview overview = overviewService.overviewByAgency(agencyOrg);
        assertEquals(1, overview.clients().size());
        ClientOverview clientOverview = overview.clients().get(0);
        assertEquals(clientOrg, clientOverview.clientOrganizationId());
        assertEquals(2, clientOverview.domainCount());

        for (DomainOverview d : clientOverview.domains()) {
            assertNull(d.healthIndication(),
                "no signal was seeded: overview health stays explicitly absent (never fabricated)");
        }

        // Verify underlying database records in PostgreSQL remain unchanged (no ownership mutation):
        Domain db1 = domainRepository.findById(registered1.id()).orElseThrow();
        Domain db2 = domainRepository.findById(registered2.id()).orElseThrow();
        assertEquals(clientOrg, db1.organizationId());
        assertEquals(clientOrg, db2.organizationId());
    }

    @Test
    void agencyPortfolioQueryDoesNotLeakOtherAgenciesClientDomainsInPostgres() {
        String agencyA = createOrg("agencyA");
        String agencyB = createOrg("agencyB");
        String clientA = createOrg("clientA");
        String clientB = createOrg("clientB");

        User ownerA = userWithRole(agencyA, Role.OWNER);
        User ownerB = userWithRole(agencyB, Role.OWNER);

        relationshipService.assign(ownerA, agencyA, clientA);
        relationshipService.assign(ownerB, agencyB, clientB);

        String domainA = PREFIX + "dom-a.example.com";
        String domainB = PREFIX + "dom-b.example.com";
        domainService.register(clientA, domainA);
        domainService.register(clientB, domainB);

        // Query Agency A portfolio:
        Portfolio portfolioA = portfolioService.findByAgency(agencyA);
        assertEquals(1, portfolioA.relationships().size());
        assertEquals(clientA, portfolioA.relationships().get(0).clientOrganizationId());

        // Query Agency B portfolio:
        Portfolio portfolioB = portfolioService.findByAgency(agencyB);
        assertEquals(1, portfolioB.relationships().size());
        assertEquals(clientB, portfolioB.relationships().get(0).clientOrganizationId());
    }
}
