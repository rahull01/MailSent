package com.mailsent.portfolio.domain.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link DomainService} — the ORG-003 application boundary
 * (register-under-organization, lookup-by-name, owner-scoped listing) against the
 * hermetic in-memory repositories.
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
}