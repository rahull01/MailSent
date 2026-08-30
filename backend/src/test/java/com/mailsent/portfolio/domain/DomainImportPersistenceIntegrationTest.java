package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mailsent.identity.authentication.domain.User;

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

import com.mailsent.portfolio.domain.application.DomainImportFailure;
import com.mailsent.portfolio.domain.application.DomainImportResult;
import com.mailsent.portfolio.domain.application.DomainImportService;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * Real-PostgreSQL integration tests for DOM-002 (Bulk domain import) over the frozen V5 schema.
 *
 * <p>Proves against live PostgreSQL:
 * <ul>
 *   <li>FUN-D2 bulk registration: all valid domains in a batch persist and link to the organization.</li>
 *   <li>Partial-success semantics: valid items persist while invalid/duplicates fail without aborting.</li>
 *   <li>INV-09 tenant isolation: imported domains are strictly scoped to the target organization.</li>
 *   <li>INV-10 global domain uniqueness: cross-org duplicates are rejected; within-batch duplicates persist once.</li>
 *   <li>INV-14 referential integrity: unknown organization is rejected; no orphan domains persist.</li>
 *   <li>N3 no silent loss: total input accounted for across imported and failures.</li>
 * </ul></p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = DomainImportPersistenceIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DomainImportPersistenceIntegrationTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
    @EntityScan(basePackageClasses = {
        OrganizationJpaEntity.class,
        DomainJpaEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        JpaOrganizationRepository.class,
        JpaDomainRepository.class
    })
    @Import({
        JpaOrganizationRepositoryAdapter.class,
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
        DomainService domainService(DomainRepository repository, OrganizationRepository organizations, Clock clock) {
            return new DomainService(repository, organizations, clock);
        }

        @Bean
        DomainImportService domainImportService(DomainService domains, OrganizationRepository organizations,
                                                com.mailsent.identity.team.application.AuthorizationService authorizationService) {
            return new DomainImportService(domains, organizations, authorizationService);
        }

        @Bean
        com.mailsent.identity.team.application.AuthorizationService authorizationService() {
            return new com.mailsent.identity.team.application.AuthorizationService();
        }
    }

    private User ownerOf(String orgId) {
        return com.mailsent.identity.authentication.domain.User.restore(
            "owner-" + orgId,
            new com.mailsent.identity.authentication.domain.Email("owner@" + UUID.randomUUID() + ".test"),
            "hash", com.mailsent.identity.authentication.domain.Role.OWNER, Instant.now(), orgId);
    }

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DomainImportService importService;
    @Autowired private DomainRepository domainRepository;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    @Test
    void allValidDomainsPersist() {
        String orgId = createdOrganization("allValidOrg");
        String d1 = PREFIX + "bulk1.example.com";
        String d2 = PREFIX + "bulk2.example.com";
        String d3 = PREFIX + "bulk3.example.com";

        DomainImportResult result = importService.importDomains(ownerOf(orgId), orgId, List.of(d1, d2, d3));

        // Contract assertions:
        assertEquals(3, result.imported().size());
        assertEquals(0, result.failures().size());

        // N3 No Silent Loss: 3 input == 3 imported + 0 failures
        assertEquals(3, result.imported().size() + result.failures().size());

        // Database assertions against live PostgreSQL:
        List<Domain> persisted = domainRepository.findByOrganizationId(orgId);
        assertEquals(3, persisted.size());
        assertTrue(persisted.stream().allMatch(d -> d.organizationId().equals(orgId)),
            "all domains belong to target org in PostgreSQL (INV-09)");
        assertEquals(List.of(d1, d2, d3), persisted.stream().map(Domain::name).sorted().toList());
    }

    @Test
    void mixedBatchPartiallySucceeds() {
        String orgId = createdOrganization("mixedOrg");
        String existingDomain = PREFIX + "already-registered.example.com";
        domainService.register(orgId, existingDomain);

        String valid1 = PREFIX + "valid-new1.example.com";
        String valid2 = PREFIX + "valid-new2.example.com";
        String blankInvalid = "   ";
        List<String> inputBatch = List.of(existingDomain, valid1, blankInvalid, valid2);

        DomainImportResult result = importService.importDomains(ownerOf(orgId), orgId, inputBatch);

        // Partial-success semantics:
        assertEquals(2, result.imported().size(), "valid entries succeeded");
        assertEquals(2, result.failures().size(), "failed entries explicitly reported");
        assertEquals(List.of(valid1, valid2), result.imported().stream().map(Domain::name).toList());

        // N3 No Silent Loss:
        assertEquals(inputBatch.size(), result.imported().size() + result.failures().size());

        // Failure reasons:
        assertTrue(result.failures().stream().anyMatch(f -> f.name().equals(existingDomain) && f.reason().equals("already registered")));
        assertTrue(result.failures().stream().anyMatch(f -> f.name().equals(blankInvalid) && f.reason().equals("invalid domain name")));

        // Direct DB verification: valid1 and valid2 are in PostgreSQL; blankInvalid is absent
        assertTrue(domainRepository.findByName(valid1).isPresent());
        assertTrue(domainRepository.findByName(valid2).isPresent());
        assertEquals(3, domainRepository.findByOrganizationId(orgId).size(), "existing + 2 valid imported");
    }

    @Test
    void duplicateAcrossOrganizations() {
        String orgA = createdOrganization("dupOrgA");
        String orgB = createdOrganization("dupOrgB");

        String sharedName = PREFIX + "cross-org-unique.example.com";
        domainService.register(orgA, sharedName);

        // Org B attempts to bulk import the same domain:
        DomainImportResult result = importService.importDomains(ownerOf(orgB), orgB, List.of(sharedName));

        assertEquals(0, result.imported().size());
        assertEquals(1, result.failures().size());
        assertEquals(sharedName, result.failures().get(0).name());
        assertEquals("already registered", result.failures().get(0).reason());

        // Direct DB verification: exactly one row in PostgreSQL owned by Org A (INV-10):
        Domain persisted = domainRepository.findByName(sharedName).orElseThrow();
        assertEquals(orgA, persisted.organizationId());
        assertEquals(0, domainRepository.findByOrganizationId(orgB).size());
    }

    @Test
    void withinBatchDuplicates() {
        String orgId = createdOrganization("withinBatchOrg");
        String baseName = PREFIX + "within-batch.example.com";
        String variation1 = "  " + baseName.toUpperCase() + "  ";
        String variation2 = baseName.toLowerCase();

        DomainImportResult result = importService.importDomains(ownerOf(orgId), orgId, List.of(variation1, variation2));

        // First variation succeeds, second variation is reported as duplicate failure:
        assertEquals(1, result.imported().size());
        assertEquals(1, result.failures().size());
        assertEquals(baseName.toLowerCase(), result.imported().get(0).name());
        assertEquals("already registered", result.failures().get(0).reason());

        // N3 No Silent Loss: 2 inputs == 1 imported + 1 failure
        assertEquals(2, result.imported().size() + result.failures().size());

        // Direct DB verification: exactly 1 row persisted with canonical lowercase name:
        List<Domain> persisted = domainRepository.findByOrganizationId(orgId);
        assertEquals(1, persisted.size());
        assertEquals(baseName.toLowerCase(), persisted.get(0).name());
    }

    @Test
    void unknownOrganization() {
        String nonExistentOrg = "org-nonexistent-" + UUID.randomUUID();
        String domainName = PREFIX + "orphan-import.example.com";

        assertThrows(DomainOrganizationNotFoundException.class,
            () -> importService.importDomains(ownerOf(nonExistentOrg), nonExistentOrg, List.of(domainName)));

        // Direct DB verification: domain was not persisted into PostgreSQL (INV-14):
        assertTrue(domainRepository.findByName(domainName).isEmpty());
    }

    @Test
    void crossOrganizationIsolation() {
        String orgA = createdOrganization("isoOrgA");
        String orgB = createdOrganization("isoOrgB");

        String da = PREFIX + "domain-a.example.com";
        String db = PREFIX + "domain-b.example.com";

        importService.importDomains(ownerOf(orgA), orgA, List.of(da));
        importService.importDomains(ownerOf(orgB), orgB, List.of(db));

        // Scoped query verification in PostgreSQL:
        List<Domain> listA = domainRepository.findByOrganizationId(orgA);
        List<Domain> listB = domainRepository.findByOrganizationId(orgB);

        assertEquals(List.of(da), listA.stream().map(Domain::name).toList());
        assertEquals(List.of(db), listB.stream().map(Domain::name).toList());
        assertTrue(listA.stream().noneMatch(d -> d.name().equals(db)), "Org A cannot see Org B domains (INV-09)");
        assertTrue(listB.stream().noneMatch(d -> d.name().equals(da)), "Org B cannot see Org A domains (INV-09)");
    }
}