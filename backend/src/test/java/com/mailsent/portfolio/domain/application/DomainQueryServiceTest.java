package com.mailsent.portfolio.domain.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.application.AuthorizationService;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link DomainQueryService} (DOM-003) against hermetic in-memory repositories.
 */
class DomainQueryServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryDomainRepository domains;
    private AuthorizationService authorizationService;
    private DomainQueryService queryService;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        domains = new InMemoryDomainRepository();
        authorizationService = new AuthorizationService(null, CLOCK);
        queryService = new DomainQueryService(domains, organizations, authorizationService);
    }

    private String createdOrg(String name) {
        return organizations.save(Organization.create(name, CLOCK.instant())).id();
    }

    private User userWithRole(String orgId, Role role) {
        return User.restore("user-" + role.name().toLowerCase(), new Email(role.name().toLowerCase() + "@test.com"),
                "hash", role, CLOCK.instant(), orgId);
    }

    private void seedDomain(String orgId, String name) {
        domains.save(Domain.register(orgId, name, CLOCK.instant()));
    }

    @Test
    void ownerCanListOwnOrganizationDomains() {
        String orgId = createdOrg("OwnerOrg");
        seedDomain(orgId, "b.example.com");
        seedDomain(orgId, "a.example.com");

        User owner = userWithRole(orgId, Role.OWNER);
        List<DomainSummary> list = queryService.listDomains(owner, orgId);

        assertEquals(2, list.size());
        assertEquals("a.example.com", list.get(0).name());
        assertEquals("b.example.com", list.get(1).name());
    }

    @Test
    void analystCanListOwnOrganizationDomains() {
        String orgId = createdOrg("AnalystOrg");
        seedDomain(orgId, "analyst.example.com");

        User analyst = userWithRole(orgId, Role.ANALYST);
        List<DomainSummary> list = queryService.listDomains(analyst, orgId);

        assertEquals(1, list.size());
        assertEquals("analyst.example.com", list.get(0).name());
    }

    @Test
    void viewerCanListOwnOrganizationDomains() {
        String orgId = createdOrg("ViewerOrg");
        seedDomain(orgId, "viewer.example.com");

        User viewer = userWithRole(orgId, Role.VIEWER);
        List<DomainSummary> list = queryService.listDomains(viewer, orgId);

        assertEquals(1, list.size());
        assertEquals("viewer.example.com", list.get(0).name());
    }

    @Test
    void unauthenticatedCallerRejectedForListing() {
        String orgId = createdOrg("TestOrg");
        assertThrows(AccessDeniedException.class,
                () -> queryService.listDomains(null, orgId));
    }

    @Test
    void crossTenantListRejected() {
        String orgA = createdOrg("OrgA");
        String orgB = createdOrg("OrgB");
        User userA = userWithRole(orgA, Role.OWNER);

        assertThrows(AccessDeniedException.class,
                () -> queryService.listDomains(userA, orgB));
    }

    @Test
    void ownDomainLookupSucceeds() {
        String orgId = createdOrg("LookupOrg");
        seedDomain(orgId, "lookup.example.com");

        User user = userWithRole(orgId, Role.VIEWER);
        Optional<DomainSummary> result = queryService.getDomain(user, orgId, "lookup.example.com");

        assertTrue(result.isPresent());
        assertEquals("lookup.example.com", result.get().name());
        assertEquals(orgId, result.get().organizationId());
    }

    @Test
    void lookupOfAnotherOrganizationsDomainReturnsEmpty() {
        String orgA = createdOrg("OrgA");
        String orgB = createdOrg("OrgB");
        seedDomain(orgB, "secret.orgb.com");

        User userA = userWithRole(orgA, Role.OWNER);
        Optional<DomainSummary> result = queryService.getDomain(userA, orgA, "secret.orgb.com");

        // INV-09: Org A cannot discover or observe Org B's domain
        assertTrue(result.isEmpty(), "Cross-tenant domain lookup must return empty (INV-09)");
    }

    @Test
    void nonexistentDomainReturnsEmpty() {
        String orgId = createdOrg("Org");
        User user = userWithRole(orgId, Role.OWNER);

        Optional<DomainSummary> result = queryService.getDomain(user, orgId, "nonexistent.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void nonexistentOrganizationRejected() {
        User user = userWithRole("missing-org", Role.OWNER);

        assertThrows(DomainOrganizationNotFoundException.class,
                () -> queryService.listDomains(user, "missing-org"));

        assertThrows(DomainOrganizationNotFoundException.class,
                () -> queryService.getDomain(user, "missing-org", "test.com"));
    }

    @Test
    void canonicalCaseInsensitiveLookupWorks() {
        String orgId = createdOrg("CaseOrg");
        seedDomain(orgId, "canonical.example.com");

        User user = userWithRole(orgId, Role.ANALYST);
        Optional<DomainSummary> result = queryService.getDomain(user, orgId, "  CANONICAL.Example.COM  ");

        assertTrue(result.isPresent());
        assertEquals("canonical.example.com", result.get().name());
    }

    @Test
    void deterministicOrderingByNameThenId() {
        String orgId = createdOrg("SortOrg");
        seedDomain(orgId, "zebra.com");
        seedDomain(orgId, "alpha.com");
        seedDomain(orgId, "beta.com");

        User user = userWithRole(orgId, Role.OWNER);
        List<DomainSummary> list = queryService.listDomains(user, orgId);

        assertEquals(List.of("alpha.com", "beta.com", "zebra.com"),
                list.stream().map(DomainSummary::name).toList());
    }

    @Test
    void returnedDtoContainsOnlyGroundedPersistedFields() {
        String orgId = createdOrg("GroundedOrg");
        seedDomain(orgId, "grounded.com");

        User user = userWithRole(orgId, Role.OWNER);
        DomainSummary summary = queryService.listDomains(user, orgId).get(0);

        assertNotNull(summary.id());
        assertEquals(orgId, summary.organizationId());
        assertEquals("grounded.com", summary.name());
        assertEquals(CLOCK.instant(), summary.createdAt());
    }
}
