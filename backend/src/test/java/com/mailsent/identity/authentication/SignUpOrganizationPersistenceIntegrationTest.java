package com.mailsent.identity.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.mailsent.identity.authentication.application.SignUpCommand;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaUserRepository;
import com.mailsent.identity.authentication.infrastructure.persistence.UserJpaEntity;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * Real-PostgreSQL integration tests for ORG-002 (User ↔ Organization atomic signup).
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V4 at boot and {@code ddl-auto: validate} verifies the schema against
 * the JPA entities, so this exercises the true path:
 * <pre>
 * SignUpService → UserRepository / OrganizationRepository → JPA adapters → PostgreSQL
 * </pre></p>
 *
 * <p>Gated on the system property {@code mailsent.it.postgres} so it runs only when a live
 * database is supplied (CI / CTO gate with {@code -Dmailsent.it.postgres=true}); the
 * hermetic default suite skips these.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class SignUpOrganizationPersistenceIntegrationTest {

    private static final String PWD = "a-sufficiently-long-password-1";

    @Autowired
    private SignUpService signUpService;
    @Autowired
    private UserRepository users;
    @Autowired
    private OrganizationRepository organizations;
    @Autowired
    private JpaUserRepository jpaUsers;
    @Autowired
    private JpaOrganizationRepository jpaOrganizations;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void successfulSignupPersistsExactlyOneOrganizationAndAssignsTheUser() {
        String email = "persist-" + UUID.randomUUID() + "@example.com";
        signUpService.signUp(new SignUpCommand(email, PWD));

        // Reload both aggregates ~ persistence-level proof of the atomic commit.
        User saved = users.findByEmail(new Email(email)).orElseThrow();
        Organization org = organizations.findById(saved.organizationId()).orElseThrow();

        assertEquals(email, saved.email().value());
        assertEquals(org.id(), saved.organizationId(),
            "the reloaded User's organization matches the reloaded Organization");
        assertEquals(Organization.Status.ACTIVE, org.status());
    }

    @Test
    void twoIndependentSignupsGetDistinctOrganizations() {
        String emailA = "tenant-a-" + UUID.randomUUID() + "@example.com";
        String emailB = "tenant-b-" + UUID.randomUUID() + "@example.com";
        signUpService.signUp(new SignUpCommand(emailA, PWD));
        signUpService.signUp(new SignUpCommand(emailB, PWD));

        User userA = users.findByEmail(new Email(emailA)).orElseThrow();
        User userB = users.findByEmail(new Email(emailB)).orElseThrow();

        assertNotEquals(userA.organizationId(), userB.organizationId(),
            "two independent signups must belong to distinct Organizations (tenant isolation)");
        assertTrue(organizations.findById(userA.organizationId()).isPresent(),
            "user A's home Organization exists in the tenant boundary");
        assertTrue(organizations.findById(userB.organizationId()).isPresent(),
            "user B's home Organization exists in the tenant boundary");
    }

    @Test
    void duplicateSignupCreatesNoAdditionalOrganization() {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        int orgsBefore = organizations.findAll().size();
        signUpService.signUp(new SignUpCommand(email, PWD));

        assertThrows(DuplicateEmailException.class,
            () -> signUpService.signUp(new SignUpCommand(email, PWD)));

        assertEquals(orgsBefore + 1, organizations.findAll().size(),
            "the only new Organization is from the successful signup; the duplicate adds none");
    }

    @Test
    void failedUserWriteRollsBackCoWrittenOrganization() {
        String email = "co-write-" + UUID.randomUUID() + "@example.com";
        // Pre-register so the email is committed and unique.
        signUpService.signUp(new SignUpCommand(email, PWD));

        String orphanId = "opkg-" + UUID.randomUUID();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // Within one transaction, write an Organization then attempt to persist a User with
        // the already-taken email -> the uk_users_email constraint fails. The whole
        // transaction (including the just-written Organization) must roll back.
        assertThrows(DataIntegrityViolationException.class, () -> tx.executeWithoutResult(status -> {
            jpaOrganizations.saveAndFlush(new OrganizationJpaEntity(
                orphanId, "Orphan Co-write", Instant.now(), Organization.Status.ACTIVE, null));
            jpaUsers.saveAndFlush(new UserJpaEntity(
                UUID.randomUUID().toString(), email, "hash", Role.VIEWER,
                Instant.now(), orphanId));
        }));

        assertTrue(organizations.findById(orphanId).isEmpty(),
            "a User write failure must roll back the co-written Organization (no orphan)");
    }
}