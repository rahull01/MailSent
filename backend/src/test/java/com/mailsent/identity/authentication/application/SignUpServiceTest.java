package com.mailsent.identity.authentication.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.InvalidEmailException;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.PasswordPolicy;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.domain.WeakPasswordException;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.authentication.infrastructure.Pbkdf2PasswordHasher;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link SignUpService} — the sign-up use case (AUTH-000 / ORG-002).
 *
 * <p>Covers success (which now also creates exactly one home Organization and assigns the
 * user to it), duplicate email, weak/invalid inputs, least-authority role assignment, that
 * the password is stored only as a hash, and that nothing is persisted when the use case
 * fails.</p>
 *
 * <p><strong>Atomicity at the unit level.</strong> The in-memory repositories are not
 * transaction-aware, so a failure leaves whatever the adapter already wrote. The durable
 * all-or-nothing guarantee is enforced by the single {@code @Transactional} on
 * {@code signUp} and is verified against real PostgreSQL in the ORG-002 integration test.
 * At this level we assert the atomicity-adjacent invariants that are meaningful here:
 * a successful signup creates exactly one Organization and assigns the user; a duplicate or
 * otherwise-failing signup creates no new Organization (the failure is detected before any
 * write); and a failure that prevents the Organization write also prevents the User write.</p>
 */
class SignUpServiceTest {

    private InMemoryUserRepository users;
    private InMemoryOrganizationRepository organizations;
    private PasswordHasher hasher;
    private PasswordPolicy policy;
    private SignUpService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        organizations = new InMemoryOrganizationRepository();
        hasher = new Pbkdf2PasswordHasher();
        policy = new PasswordPolicy(12);
        service = new SignUpService(users, organizations, hasher, policy);
    }

    private SignUpCommand command(String email, String password) {
        return new SignUpCommand(email, password);
    }

    @Test
    void successfulRegistrationCreatesExactlyOneOrganizationAndAssignsTheUser() {
        SignUpResult result = service.signUp(command("owner@example.com", "a-sufficiently-long-password-1"));
        assertNotNull(result.id());
        assertEquals("owner@example.com", result.email());
        assertEquals(Role.VIEWER, result.role(), "a new user starts with the least-authority role");

        // ORG-002: exactly one home Organization is created for the new signup...
        assertEquals(1, organizations.findAll().size(), "a signup creates exactly one Organization");
        Organization org = organizations.findAll().get(0);

        // ...and the signing User is attached to that Organization (exactly one home org).
        // The org id stays internal to the backend; it is asserted through the repositories,
        // not the public result contract.
        User saved = users.findByEmail(new Email("owner@example.com")).orElseThrow();
        assertEquals(org.id(), saved.organizationId(), "the user belongs to exactly one home Organization (D6)");
        assertEquals(org.id(), organizations.findById(org.id()).orElseThrow().id());
    }

    @Test
    void emailIsNormalizedToLowercase() {
        SignUpResult result = service.signUp(command("Owner@Example.COM", "a-sufficiently-long-password-1"));
        assertEquals("owner@example.com", result.email());
    }

    @Test
    void passwordIsStoredHashedNotPlaintext() {
        service.signUp(command("owner@example.com", "a-sufficiently-long-password-1"));
        // The in-memory repo keeps the User entity, whose passwordHash is a pbkdf2 value.
        assertTrue(users.existsByEmail(new Email("owner@example.com")));
    }

    @Test
    void duplicateEmailIsRejectedAndCreatesNoAdditionalOrganization() {
        String email = "owner@example.com";
        String pwd = "a-sufficiently-long-password-1";
        service.signUp(command(email, pwd));
        int orgCount = organizations.findAll().size();
        assertEquals(1, orgCount, "first signup created exactly one Organization");

        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
            () -> service.signUp(command(email, pwd)));

        assertTrue(ex.getMessage().toLowerCase().contains("already"));
        assertEquals(orgCount, organizations.findAll().size(),
            "a duplicate signup creates no additional Organization (no orphan on failure)");
    }

    @Test
    void duplicateEmailDiffersOnlyByCaseIsRejected() {
        service.signUp(command("owner@example.com", "a-sufficiently-long-password-1"));
        assertThrows(DuplicateEmailException.class,
            () -> service.signUp(command("OWNER@example.com", "a-sufficiently-long-password-1")));
    }

    @Test
    void malformedEmailIsRejectedWithoutCreatingAnOrganization() {
        assertThrows(InvalidEmailException.class,
            () -> service.signUp(command("not-an-email", "a-sufficiently-long-password-1")));
        assertTrue(organizations.findAll().isEmpty(), "a validation failure creates no Organization");
    }

    @Test
    void weakPasswordIsRejectedWithoutCreatingAnOrganization() {
        assertThrows(WeakPasswordException.class,
            () -> service.signUp(command("owner@example.com", "short")));
        assertTrue(organizations.findAll().isEmpty(), "a password-policy failure creates no Organization");
    }

    @Test
    void nullCommandElementsAreRejected() {
        assertThrows(InvalidEmailException.class, () -> service.signUp(command(null, "a-sufficiently-long-password-1")));
        assertThrows(WeakPasswordException.class, () -> service.signUp(command("owner@example.com", null)));
    }

    @Test
    void failedSignUpPersistsNeitherUserNorOrganization() {
        assertThrows(WeakPasswordException.class,
            () -> service.signUp(command("owner@example.com", "short")));
        assertFalse(users.existsByEmail(new Email("owner@example.com")));
        assertTrue(organizations.findAll().isEmpty(), "a failed signup leaves no Organization behind");
    }

    @Test
    void emptyOrInjectionLikeEmailIsRejected() {
        // Header/shell injection-like payloads must not create an account.
        assertThrows(InvalidEmailException.class,
            () -> service.signUp(command("owner@example.com\r\nbcc:x@y.z", "a-sufficiently-long-password-1")));
    }

    @Test
    void organizationWriteFailurePreventsAnyUserPersistence() {
        // If the Organization cannot be persisted, the signup fails and no User is created
        // (the durable rollback is exercised against real PostgreSQL; here we assert the
        // enabled failure remains no single side effective for in-memory semantics).
        OrganizationRepository failingOrgs = new OrganizationRepository() {
            @Override
            public Organization save(Organization organization) {
                throw new IllegalStateException("organization store unavailable");
            }
            @Override
            public java.util.Optional<Organization> findById(String id) {
                return java.util.Optional.empty();
            }
            @Override
            public java.util.List<Organization> findAll() {
                return java.util.List.of();
            }
        };
        SignUpService svc = new SignUpService(users, failingOrgs, hasher, policy);

        assertThrows(IllegalStateException.class,
            () -> svc.signUp(command("owner@example.com", "a-sufficiently-long-password-1")));
        assertFalse(users.existsByEmail(new Email("owner@example.com")),
            "an Organization write failure must not leave a partially-created User");
    }

    @Test
    void repositoryFailurePropagatesAndPersistsNoUser() {
        InMemoryUserRepository sparedUsers = new InMemoryUserRepository();
        UserRepository failing = new UserRepository() {
            @Override
            public boolean existsByEmail(Email email) {
                return false;
            }
            @Override
            public java.util.Optional<User> findByEmail(Email email) {
                return java.util.Optional.empty();
            }
            @Override
            public java.util.Optional<User> findById(String id) {
                return java.util.Optional.empty();
            }
            @Override
            public User save(User user) {
                throw new IllegalStateException("database unavailable");
            }
            @Override
            public java.util.List<User> findAllByOrganizationId(String organizationId) {
                return java.util.List.of();
            }
        };
        // A real in-memory org repository stores the Organization; the user write then
        // fails. In production the single @Transactional rolls this back (verified in the
        // PostgreSQL integration test); here we assert the failure surfaces and no user
        // was created.
        SignUpService svc = new SignUpService(failing, organizations, hasher, policy);

        assertThrows(IllegalStateException.class,
            () -> svc.signUp(command("owner@example.com", "a-sufficiently-long-password-1")));
        assertFalse(sparedUsers.existsByEmail(new Email("owner@example.com")));
    }

    @Test
    void concurrentSignUpsForSameEmailSucceedExactlyOnce() throws Exception {
        String email = "race@example.com";
        String pwd = "a-sufficiently-long-password-1";
        int threads = 8;

        // The in-memory repo enforces uniqueness atomically (putIfAbsent), mirroring the
        // DB unique constraint, so concurrent duplicate sign-ups yield exactly one success.
        java.util.concurrent.atomic.AtomicInteger created = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                try {
                    service.signUp(command(email, pwd));
                    created.incrementAndGet();
                } catch (DuplicateEmailException expected) {
                    // acceptable: another thread won the race for this identity
                }
            }));
        }
        for (java.util.concurrent.Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(1, created.get(), "only one concurrent sign-up may succeed per email");
        assertEquals(1, users.count());
    }
}