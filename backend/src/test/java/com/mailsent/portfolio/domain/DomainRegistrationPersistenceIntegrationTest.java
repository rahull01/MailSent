package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
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

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * Real-PostgreSQL integration tests for DOM-001 (Manual domain registration) over the
 * frozen V5 schema.
 *
 * <p>Proves against live PostgreSQL:
 * <ul>
 *   <li>Valid domain registration persists and associates with caller's organization.</li>
 *   <li>Normalized hostname (trimmed and lowercased) is persisted.</li>
 *   <li>Duplicate domain registration is rejected (same organization and cross-organization).</li>
 *   <li>Global domain uniqueness (INV-10) is enforced.</li>
 *   <li>Nonexistent organization is rejected and FK integrity is maintained (INV-14).</li>
 *   <li>Cross-organization caller is rejected (INV-09).</li>
 * </ul></p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = DomainRegistrationPersistenceIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DomainRegistrationPersistenceIntegrationTest {

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
    }

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DomainRepository domainRepository;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    private User userWithRole(String orgId, Role role) {
        return User.restore(UUID.randomUUID().toString(), new Email("owner@" + PREFIX + "example.com"),
            "hash", role, Instant.now(), orgId);
    }

    @Test
    void validDomainRegistrationPersistsAndAssociatesWithOrganization() {
        String orgId = createdOrganization("regOrg");
        User owner = userWithRole(orgId, Role.OWNER);

        String domainName = PREFIX + "valid.example.com";
        Domain registered = domainService.register(owner, orgId, domainName);

        assertNotNull(registered.id());
        assertEquals(domainName.toLowerCase(), registered.name());
        assertEquals(orgId, registered.organizationId());

        Domain loaded = domainRepository.findById(registered.id()).orElseThrow();
        assertEquals(registered.name(), loaded.name());
        assertEquals(orgId, loaded.organizationId());
    }

    @Test
    void normalizedHostnameIsPersistedInPostgres() {
        String orgId = createdOrganization("normOrg");
        User owner = userWithRole(orgId, Role.OWNER);

        String rawInput = "  " + PREFIX.toUpperCase() + "NORM.EXAMPLE.COM  ";
        Domain registered = domainService.register(owner, orgId, rawInput);

        String expectedNormalized = (PREFIX + "norm.example.com").toLowerCase();
        assertEquals(expectedNormalized, registered.name());

        Domain loaded = domainRepository.findByName(expectedNormalized).orElseThrow();
        assertEquals(expectedNormalized, loaded.name());
    }

    @Test
    void duplicateRegistrationIsRejectedCaseInsensitivelyInSameOrg() {
        String orgId = createdOrganization("dupOrg1");
        User owner = userWithRole(orgId, Role.OWNER);

        String domainName = PREFIX + "dup.example.com";
        domainService.register(owner, orgId, domainName);

        assertThrows(DomainAlreadyExistsException.class,
            () -> domainService.register(owner, orgId, domainName.toUpperCase()));
    }

    @Test
    void globalUniquenessIsEnforcedAcrossDifferentOrganizations() {
        String orgA = createdOrganization("globalOrgA");
        String orgB = createdOrganization("globalOrgB");
        User ownerA = userWithRole(orgA, Role.OWNER);
        User ownerB = userWithRole(orgB, Role.OWNER);

        String domainName = PREFIX + "global-unique.example.com";
        domainService.register(ownerA, orgA, domainName);

        // Org B attempts to register the exact same domain name (INV-10)
        assertThrows(DomainAlreadyExistsException.class,
            () -> domainService.register(ownerB, orgB, domainName));
    }

    @Test
    void missingOrganizationIsRejected() {
        String nonExistentOrg = "org-nonexistent-" + UUID.randomUUID();
        User owner = userWithRole(nonExistentOrg, Role.OWNER);

        assertThrows(DomainOrganizationNotFoundException.class,
            () -> domainService.register(owner, nonExistentOrg, PREFIX + "orphan.example.com"));
    }

    @Test
    void crossOrganizationCallerCannotRegisterDomain() {
        String orgA = createdOrganization("crossOrgA");
        String orgB = createdOrganization("crossOrgB");
        User ownerA = userWithRole(orgA, Role.OWNER);

        // Owner of Org A attempts to register a domain for Org B (INV-09)
        assertThrows(AccessDeniedException.class,
            () -> domainService.register(ownerA, orgB, PREFIX + "cross.example.com"));
    }
}
