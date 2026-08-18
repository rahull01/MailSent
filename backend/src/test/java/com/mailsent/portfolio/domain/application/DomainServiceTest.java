package com.mailsent.portfolio.domain.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link DomainService} — the ORG-003 / DOM-001 application boundary
 * (register-under-organization, authenticated capability registration, lookup-by-name,
 * owner-scoped listing) against the hermetic in-memory repositories.
 */
class DomainServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryDomainRepository domains;
    private DomainService service;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        domains = new InMemoryDomainRepository();
        service = new DomainService(domains, organizations, CLOCK);
    }

    private String createdOrgId() {
        return new OrganizationService(organizations, CLOCK).create("Acme").id();
    }

    private User userWithRole(String orgId, Role role) {
        return User.restore("user-1", new Email("owner@acme.com"), "hash", role, CLOCK.instant(), orgId);
    }

    // --- Unauthenticated / internal registration tests (ORG-003) ---

    @Test
    void registerPersistsDomainUnderOrganization() {
        String orgId = createdOrgId();
        Domain domain = service.register(orgId, "example.com");

        assertEquals("example.com", domain.name());
        assertEquals(orgId, domain.organizationId());
        assertTrue(domains.findById(domain.id()).isPresent());
    }

    @Test
    void registerRejectsUnknownOrganization() {
        assertThrows(DomainOrganizationNotFoundException.class,
            () -> service.register("missing-org", "example.com"));
    }

    @Test
    void registerRejectsDuplicateDomainNameCaseInsensitively() {
        String orgId = createdOrgId();
        service.register(orgId, "example.com");
        assertThrows(DomainAlreadyExistsException.class,
            () -> service.register(orgId, "Example.COM"));
    }

    @Test
    void findByDomainFoldsCase() {
        String orgId = createdOrgId();
        service.register(orgId, "Example.COM");

        assertTrue(service.findByDomain("example.com").isPresent());
        assertFalse(service.findByDomain("nope.com").isPresent());
    }

    @Test
    void findByOrganizationIdListsOnlyThatOrganizationsDomains() {
        String orgA = createdOrgId();
        String orgB = createdOrgId();
        service.register(orgA, "a.com");
        service.register(orgA, "b.com");
        service.register(orgB, "c.com");

        assertEquals(2, service.findByOrganizationId(orgA).size());
        assertEquals(1, service.findByOrganizationId(orgB).size());
    }

    // --- Authenticated / DOM-001 Capability & INV-09 tests ---

    @Test
    void authenticatedOwnerCanRegisterDomainInOwnOrganization() {
        String orgId = createdOrgId();
        User owner = userWithRole(orgId, Role.OWNER);

        Domain domain = service.register(owner, orgId, "mail.example.com");

        assertNotNull(domain.id());
        assertEquals("mail.example.com", domain.name());
        assertEquals(orgId, domain.organizationId());
        assertTrue(domains.findById(domain.id()).isPresent());
    }

    @Test
    void authenticatedRegistrationNormalizesDomainName() {
        String orgId = createdOrgId();
        User owner = userWithRole(orgId, Role.OWNER);

        Domain domain = service.register(owner, orgId, "  Sub.Example.COM  ");

        assertEquals("sub.example.com", domain.name());
    }

    @Test
    void authenticatedRegistrationRejectsUnauthenticatedCaller() {
        String orgId = createdOrgId();
        assertThrows(AccessDeniedException.class,
            () -> service.register(null, orgId, "example.com"));
    }

    @Test
    void authenticatedRegistrationRejectsCrossTenantCaller() {
        String orgA = createdOrgId();
        String orgB = createdOrgId();
        User ownerOfOrgA = userWithRole(orgA, Role.OWNER);

        // Attempting to register domain for Org B when caller is from Org A (INV-09)
        assertThrows(AccessDeniedException.class,
            () -> service.register(ownerOfOrgA, orgB, "example.com"));
    }

    @Test
    void authenticatedRegistrationRejectsNonOwnerRoles() {
        String orgId = createdOrgId();
        User analyst = userWithRole(orgId, Role.ANALYST);
        User viewer = userWithRole(orgId, Role.VIEWER);

        assertThrows(AccessDeniedException.class,
            () -> service.register(analyst, orgId, "analyst-domain.com"));

        assertThrows(AccessDeniedException.class,
            () -> service.register(viewer, orgId, "viewer-domain.com"));
    }

    @Test
    void authenticatedRegistrationRejectsDuplicateDomainAcrossSameOrDifferentOrgs() {
        String orgA = createdOrgId();
        String orgB = createdOrgId();
        User ownerA = userWithRole(orgA, Role.OWNER);
        User ownerB = userWithRole(orgB, Role.OWNER);

        service.register(ownerA, orgA, "unique.com");

        // Same org duplicate
        assertThrows(DomainAlreadyExistsException.class,
            () -> service.register(ownerA, orgA, "UNIQUE.COM"));

        // Cross-org duplicate (INV-10 global uniqueness)
        assertThrows(DomainAlreadyExistsException.class,
            () -> service.register(ownerB, orgB, "unique.com"));
    }

    @Test
    void authenticatedRegistrationRejectsNonexistentOrganization() {
        User owner = userWithRole("missing-org", Role.OWNER);
        assertThrows(DomainOrganizationNotFoundException.class,
            () -> service.register(owner, "missing-org", "example.com"));
    }

    @Test
    void authenticatedRegistrationRejectsBlankOrNullDomainNames() {
        String orgId = createdOrgId();
        User owner = userWithRole(orgId, Role.OWNER);

        assertThrows(IllegalArgumentException.class,
            () -> service.register(owner, orgId, ""));

        assertThrows(IllegalArgumentException.class,
            () -> service.register(owner, orgId, "   "));

        assertThrows(IllegalArgumentException.class,
            () -> service.register(owner, orgId, null));
    }
}