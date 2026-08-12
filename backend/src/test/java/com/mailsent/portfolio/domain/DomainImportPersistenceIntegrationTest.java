package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainImportResult;
import com.mailsent.portfolio.domain.application.DomainImportService;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the DOM-002 bulk domain import.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}); Flyway
 * applies V1–V7 at startup. Exercises the true path
 * {@code DomainImportService → DomainService → JPA adapter → PostgreSQL → reload}, the
 * global-unique backstop, and cross-organization isolation (INV-09). Gated on
 * {@code mailsent.it.postgres}; idempotent (randomized org/domain names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DomainImportPersistenceIntegrationTest {

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domains;
    @Autowired private DomainImportService domainImport;

    // A per-method prefix keeps each test idempotent against the shared, persistent schema:
    // domain names are globally UNIQUE, so two tests must never reuse a name.
    private String prefix() {
        return "it-" + UUID.randomUUID() + "-";
    }

    private String createdOrganization(String label) {
        return organizationService.create(prefix() + label).id();
    }

    @Test
    void importPersistsManyDomainsForOneOrganization() {
        String PREFIX = prefix();
        String orgId = createdOrganization("org");
        DomainImportResult result = domainImport.importDomains(orgId,
            List.of(PREFIX + "a.com", PREFIX + "b.com", PREFIX + "c.com"));

        assertEquals(3, result.imported().size());
        assertEquals(0, result.failures().size());
        assertEquals(3, domains.findByOrganizationId(orgId).size(),
            "all imported domains persisted and owned by the organization");
        assertTrue(domains.findByOrganizationId(orgId).stream()
            .allMatch(d -> d.organizationId().equals(orgId)));
    }

    @Test
    void importReportsGlobalDuplicateAndInvalidNames() {
        String PREFIX = prefix();
        String orgId = createdOrganization("org");
        domains.register(orgId, PREFIX + "existing.com");
        DomainImportResult result = domainImport.importDomains(orgId,
            List.of(PREFIX + "existing.com", PREFIX + "new.com", "   ", PREFIX + "other.com"));

        assertEquals(List.of(PREFIX + "new.com", PREFIX + "other.com"),
            result.imported().stream().map(d -> d.name()).toList());
        assertEquals(2, result.failures().size());
        assertTrue(result.failures().stream().anyMatch(f -> f.reason().equals("already registered")));
        assertTrue(result.failures().stream().anyMatch(f -> f.reason().equals("invalid domain name")));
    }

    @Test
    void withinBatchDuplicateImportsOnce() {
        String PREFIX = prefix();
        String orgId = createdOrganization("org");
        DomainImportResult result = domainImport.importDomains(orgId, List.of(PREFIX + "x.com", PREFIX + "x.com"));
        assertEquals(1, result.imported().size(), "a duplicate within the batch imports once");
        assertEquals(1, result.failures().size());
    }

    @Test
    void importIsIsolatedAcrossOrganizations() {
        String PREFIX = prefix();
        String orgA = createdOrganization("orgA");
        String orgB = createdOrganization("orgB");
        domainImport.importDomains(orgA, List.of(PREFIX + "a.com"));
        domainImport.importDomains(orgB, List.of(PREFIX + "b.com"));

        assertTrue(domains.findByOrganizationId(orgA).stream()
            .noneMatch(d -> d.name().equals(PREFIX + "b.com")),
            "an import for org B never appears in org A's registry (INV-09)");
        assertTrue(domains.findByOrganizationId(orgA).stream()
            .allMatch(d -> d.organizationId().equals(orgA)));
    }
}