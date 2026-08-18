package com.mailsent.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
import com.mailsent.identity.team.application.AuthorizationService;
import com.mailsent.portfolio.domain.application.DomainQueryService;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.application.DomainSummary;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * Real-PostgreSQL integration tests for DOM-003 (Domain list and state query) over the frozen V5 schema.
 *
 * <p>Proves against live PostgreSQL:
 * <ul>
 *   <li>Org A listing contains strictly Org A domains.</li>
 *   <li>Org B listing contains strictly Org B domains.</li>
 *   <li>Org A cannot enumerate or observe Org B's domain through single lookup (INV-09).</li>
 *   <li>Canonical domain names and persisted metadata are returned accurately (N2).</li>
 *   <li>Deterministic ordering is preserved.</li>
 *   <li>Empty organization returns an empty list.</li>
 * </ul></p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = DomainListPersistenceIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DomainListPersistenceIntegrationTest {

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
        AuthorizationService authorizationService(Clock clock) {
            return new AuthorizationService(null, clock);
        }

        @Bean
        DomainQueryService domainQueryService(DomainRepository repository,
                                              OrganizationRepository organizations,
                                              AuthorizationService authorizationService) {
            return new DomainQueryService(repository, organizations, authorizationService);
        }
    }

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DomainQueryService domainQueryService;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    private User userWithRole(String orgId, Role role) {
        return User.restore(UUID.randomUUID().toString(), new Email("user@" + PREFIX + "test.com"),
                "hash", role, Instant.now(), orgId);
    }

    @Test
    void listDomainsReturnsOnlyOwnOrganizationDomainsInPostgres() {
        String orgA = createdOrganization("listOrgA");
        String orgB = createdOrganization("listOrgB");

        String da1 = PREFIX + "a-one.example.com";
        String da2 = PREFIX + "a-two.example.com";
        String db1 = PREFIX + "b-one.example.com";

        domainService.register(orgA, da2);
        domainService.register(orgA, da1);
        domainService.register(orgB, db1);

        User userA = userWithRole(orgA, Role.VIEWER);
        User userB = userWithRole(orgB, Role.ANALYST);

        List<DomainSummary> listA = domainQueryService.listDomains(userA, orgA);
        List<DomainSummary> listB = domainQueryService.listDomains(userB, orgB);

        // Org A isolation:
        assertEquals(2, listA.size());
        assertEquals(List.of(da1, da2), listA.stream().map(DomainSummary::name).toList(), "deterministic name sorting");
        assertTrue(listA.stream().allMatch(d -> d.organizationId().equals(orgA)));

        // Org B isolation:
        assertEquals(1, listB.size());
        assertEquals(db1, listB.get(0).name());
        assertEquals(orgB, listB.get(0).organizationId());

        // Zero cross-tenant leakage (INV-09):
        assertTrue(listA.stream().noneMatch(d -> d.name().equals(db1)));
        assertTrue(listB.stream().noneMatch(d -> d.name().equals(da1)));
    }

    @Test
    void singleDomainLookupEnforcesTenantBoundaryInPostgres() {
        String orgA = createdOrganization("lookupOrgA");
        String orgB = createdOrganization("lookupOrgB");

        String secretDomain = PREFIX + "secret.example.com";
        domainService.register(orgB, secretDomain);

        User userA = userWithRole(orgA, Role.OWNER);
        User userB = userWithRole(orgB, Role.OWNER);

        // Org B can look up its own domain:
        Optional<DomainSummary> foundForB = domainQueryService.getDomain(userB, orgB, secretDomain);
        assertTrue(foundForB.isPresent());
        assertEquals(secretDomain, foundForB.get().name());
        assertEquals(orgB, foundForB.get().organizationId());
        assertNotNull(foundForB.get().createdAt());

        // Org A cannot look up Org B's domain (INV-09 returns empty):
        Optional<DomainSummary> foundForA = domainQueryService.getDomain(userA, orgA, secretDomain);
        assertTrue(foundForA.isEmpty(), "Cross-tenant domain query must return empty");
    }

    @Test
    void canonicalDomainNameAndPersistedMetadataAreAccurate() {
        String orgId = createdOrganization("canonOrg");
        String rawName = "  " + PREFIX.toUpperCase() + "CANON.EXAMPLE.COM  ";
        Domain registered = domainService.register(orgId, rawName);

        User user = userWithRole(orgId, Role.OWNER);
        DomainSummary summary = domainQueryService.getDomain(user, orgId, rawName).orElseThrow();

        assertEquals(registered.id(), summary.id());
        assertEquals((PREFIX + "canon.example.com").toLowerCase(), summary.name());
        assertEquals(orgId, summary.organizationId());
        assertNotNull(summary.createdAt());
    }

    @Test
    void emptyOrganizationReturnsEmptyList() {
        String orgId = createdOrganization("emptyOrg");
        User user = userWithRole(orgId, Role.VIEWER);

        List<DomainSummary> list = domainQueryService.listDomains(user, orgId);
        assertTrue(list.isEmpty());
    }
}
