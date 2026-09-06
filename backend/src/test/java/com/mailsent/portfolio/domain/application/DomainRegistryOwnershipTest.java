package com.mailsent.portfolio.domain.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.application.AuthorizationService;
import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.infrastructure.InMemoryClientRelationshipRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;
import com.mailsent.portfolio.portfolio.application.PortfolioOverviewService;
import com.mailsent.portfolio.portfolio.application.PortfolioService;
import com.mailsent.portfolio.portfolio.domain.ClientOverview;
import com.mailsent.portfolio.portfolio.domain.DomainOverview;
import com.mailsent.portfolio.portfolio.domain.Portfolio;
import com.mailsent.portfolio.portfolio.domain.PortfolioOverview;

/**
 * Hermetic unit tests for DOM-004 (Domain registry ownership and portfolio interaction invariants)
 * under FUN-D4, INV-09, INV-10, INV-14, and N2.
 */
class DomainRegistryOwnershipTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryDomainRepository domains;
    private InMemoryClientRelationshipRepository clientRelationships;
    private DomainService domainService;
    private DomainImportService importService;
    private ClientRelationshipService relationshipService;
    private PortfolioService portfolioService;
    private PortfolioOverviewService overviewService;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        domains = new InMemoryDomainRepository();
        clientRelationships = new InMemoryClientRelationshipRepository();
        domainService = new DomainService(domains, organizations, CLOCK);
        importService = new DomainImportService(domainService, organizations, new AuthorizationService());
        relationshipService = new ClientRelationshipService(clientRelationships, organizations, CLOCK);
        portfolioService = new PortfolioService(clientRelationships, organizations);
        overviewService = new PortfolioOverviewService(clientRelationships, domains, organizations,
            mock(HealthSignalService.class));
    }

    private String createOrg(String name) {
        return organizations.save(Organization.create(name, CLOCK.instant())).id();
    }

    private User userWithRole(String orgId, Role role) {
        return User.restore("user-1", new Email("owner@test.com"), "hash", role, CLOCK.instant(), orgId);
    }

    @Test
    void domainAggregateEnforcesMandatoryOwningOrganization() {
        assertThrows(IllegalArgumentException.class,
                () -> Domain.register(null, "example.com", CLOCK.instant()));

        assertThrows(IllegalArgumentException.class,
                () -> Domain.register("   ", "example.com", CLOCK.instant()));
    }

    @Test
    void domainAggregateHasNoMutableOwnershipSetters() {
        // Enforce ownership immutability: Domain has no setOrganizationId or setOwner methods
        List<String> setterNames = Arrays.stream(Domain.class.getMethods())
                .map(Method::getName)
                .filter(name -> name.startsWith("set"))
                .toList();

        assertTrue(setterNames.isEmpty(), "Domain entity must remain strictly immutable (no setters)");
    }

    @Test
    void domainRegistrationRejectsNonexistentOrganization() {
        User owner = userWithRole("missing-org", Role.OWNER);

        assertThrows(DomainOrganizationNotFoundException.class,
                () -> domainService.register(owner, "missing-org", "example.com"));

        assertThrows(DomainOrganizationNotFoundException.class,
                () -> domainService.register("missing-org", "example.com"));
    }

    @Test
    void domainImportPreservesTargetOrganizationOwnership() {
        String orgId = createOrg("ImportOrg");
        DomainImportResult result = importService.importDomains(userWithRole(orgId, Role.OWNER), orgId, List.of("one.com", "two.com"));

        assertEquals(2, result.imported().size());
        assertTrue(result.imported().stream().allMatch(d -> d.organizationId().equals(orgId)),
                "Imported domains must strictly belong to target organization (INV-09)");
    }

    @Test
    void duplicateRegistrationAcrossOrganizationsFails() {
        String orgA = createOrg("OrgA");
        String orgB = createOrg("OrgB");
        User ownerA = userWithRole(orgA, Role.OWNER);
        User ownerB = userWithRole(orgB, Role.OWNER);

        domainService.register(ownerA, orgA, "shared.com");

        assertThrows(DomainAlreadyExistsException.class,
                () -> domainService.register(ownerB, orgB, "shared.com"),
                "Domain cannot be registered by multiple organizations (INV-10)");
    }

    @Test
    void portfolioAggregationPreservesClientDomainOwnershipWithoutMutatingOrTransferring() {
        String agencyOrg = createOrg("AgencyOrg");
        String clientOrg = createOrg("ClientOrg");
        User agencyOwner = userWithRole(agencyOrg, Role.OWNER);

        // Assign client to agency:
        relationshipService.assign(agencyOwner, agencyOrg, clientOrg);

        // Client registers domains:
        Domain d1 = domainService.register(clientOrg, "client-d1.com");
        Domain d2 = domainService.register(clientOrg, "client-d2.com");

        // Agency views portfolio (PORT-001):
        Portfolio portfolio = portfolioService.findByAgency(agencyOrg);
        assertEquals(1, portfolio.relationships().size());
        assertEquals(clientOrg, portfolio.relationships().get(0).clientOrganizationId());

        // Agency views overview (PORT-002):
        PortfolioOverview overview = overviewService.overviewByAgency(agencyOrg);
        assertEquals(1, overview.clients().size());
        ClientOverview clientOverview = overview.clients().get(0);
        assertEquals(clientOrg, clientOverview.clientOrganizationId());
        assertEquals(2, clientOverview.domainCount());

        // Domain ownership remains ClientOrg (NOT AgencyOrg); no signal seeded → absent health:
        for (DomainOverview dom : clientOverview.domains()) {
            assertNull(dom.healthIndication(),
                "no Health Signal was seeded: overview health is explicitly absent (never fabricated)");
        }

        // Confirm database/repository domain records were NOT mutated:
        Domain persisted1 = domains.findById(d1.id()).orElseThrow();
        Domain persisted2 = domains.findById(d2.id()).orElseThrow();
        assertEquals(clientOrg, persisted1.organizationId());
        assertEquals(clientOrg, persisted2.organizationId());
    }

    @Test
    void agencyCannotSeeUnassignedClientDomainsInPortfolio() {
        String agencyA = createOrg("AgencyA");
        String agencyB = createOrg("AgencyB");
        String clientB = createOrg("ClientB");
        User ownerB = userWithRole(agencyB, Role.OWNER);

        // Agency B is assigned Client B:
        relationshipService.assign(ownerB, agencyB, clientB);
        domainService.register(clientB, "b-client-domain.com");

        // Agency A queries portfolio:
        Portfolio portfolioA = portfolioService.findByAgency(agencyA);
        assertTrue(portfolioA.relationships().isEmpty(), "Agency A has no clients (INV-09)");

        PortfolioOverview overviewA = overviewService.overviewByAgency(agencyA);
        assertTrue(overviewA.clients().isEmpty(), "Agency A sees 0 client overviews (INV-09)");
    }
}
