package com.mailsent.portfolio.domain.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link DomainImportService} (DOM-002) against the hermetic in-memory
 * repositories: bulk registration of many domains for one organization, partial import with
 * per-name failures, and cross-organization isolation.
 */
class DomainImportServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryDomainRepository domainRepository;
    private DomainService domainService;
    private DomainImportService importService;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        domainRepository = new InMemoryDomainRepository();
        domainService = new DomainService(domainRepository, organizations, CLOCK);
        importService = new DomainImportService(domainService, organizations);
    }

    private String createdOrganization(String name) {
        return organizations.save(Organization.create(name, CLOCK.instant())).id();
    }

    @Test
    void importsAllValidNamesForOneOrganization() {
        String orgId = createdOrganization("Acme");
        DomainImportResult result = importService.importDomains(orgId, List.of("a.com", "b.com", "c.com"));

        assertEquals(3, result.imported().size(), "all three names imported");
        assertEquals(0, result.failures().size());
        assertTrue(result.imported().stream().allMatch(d -> d.organizationId().equals(orgId)),
            "every imported domain is owned by the target organization (INV-09)");
        assertEquals(3, domainRepository.findByOrganizationId(orgId).size(), "persisted and reloadable");
    }

    @Test
    void importPreservesOfferedOrder() {
        String orgId = createdOrganization("Acme");
        DomainImportResult result = importService.importDomains(orgId, List.of("a.com", "b.com"));
        assertEquals(List.of("a.com", "b.com"),
            result.imported().stream().map(Domain::name).toList());
    }

    @Test
    void mixedBatchImportsValidAndReportsFailures() {
        String orgId = createdOrganization("Acme");
        domainService.register(orgId, "existing.com");
        DomainImportResult result = importService.importDomains(orgId,
            List.of("existing.com", "new.com", "   ", "other.com"));

        assertEquals(List.of("new.com", "other.com"),
            result.imported().stream().map(Domain::name).toList());
        assertEquals(2, result.failures().size());
        assertEquals(List.of("existing.com", "   "),
            result.failures().stream().map(DomainImportFailure::name).toList());
        assertTrue(result.failures().stream().anyMatch(f -> f.name().equals("existing.com")
            && f.reason().equals("already registered")));
        assertTrue(result.failures().stream().anyMatch(f -> f.name().equals("   ")
            && f.reason().equals("invalid domain name")));
    }

    @Test
    void withinBatchDuplicateImportsOnce() {
        String orgId = createdOrganization("Acme");
        DomainImportResult result = importService.importDomains(orgId, List.of("a.com", "a.com"));

        assertEquals(1, result.imported().size(), "a duplicate within the batch is imported once");
        assertEquals(1, result.failures().size());
        assertEquals("already registered", result.failures().get(0).reason());
    }

    @Test
    void importDoesNotCrossOrganizations() {
        String orgA = createdOrganization("Acme A");
        String orgB = createdOrganization("Acme B");
        importService.importDomains(orgA, List.of("a.com"));
        importService.importDomains(orgB, List.of("b.com"));

        assertTrue(domainRepository.findByOrganizationId(orgA).stream()
            .allMatch(d -> d.organizationId().equals(orgA)));
        assertTrue(domainRepository.findByOrganizationId(orgB).stream()
            .allMatch(d -> d.organizationId().equals(orgB)));
        assertTrue(domainRepository.findByOrganizationId(orgA).stream().noneMatch(d -> d.name().equals("b.com")),
            "an import for org B never registers a domain for org A (INV-09)");
    }

    @Test
    void emptyBatchImportsNothing() {
        String orgId = createdOrganization("Acme");
        DomainImportResult result = importService.importDomains(orgId, List.of());
        assertTrue(result.imported().isEmpty());
        assertTrue(result.failures().isEmpty());
    }

    @Test
    void importRejectsUnknownOrganization() {
        assertThrows(DomainOrganizationNotFoundException.class,
            () -> importService.importDomains("no-such-org", List.of("a.com")));
    }
}