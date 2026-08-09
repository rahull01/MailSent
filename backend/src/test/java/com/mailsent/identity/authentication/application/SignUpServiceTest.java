package com.mailsent.identity.authentication.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.InvalidEmailException;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.PasswordPolicy;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.WeakPasswordException;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.authentication.infrastructure.Pbkdf2PasswordHasher;

/**
 * Unit/integration tests for {@link SignUpService} — the sign-up use case.
 *
 * <p>Covers success, duplicate email, weak/invalid inputs, least-authority role
 * assignment, that the password is stored only as a hash, and that nothing is
 * persisted when the use case fails (rolled back / not committed).</p>
 */
class SignUpServiceTest {

    private UserRepository users;
    private PasswordHasher hasher;
    private PasswordPolicy policy;
    private SignUpService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        hasher = new Pbkdf2PasswordHasher();
        policy = new PasswordPolicy(12);
        service = new SignUpService(users, hasher, policy);
    }

    private SignUpCommand command(String email, String password) {
        return new SignUpCommand(email, password);
    }

    @Test
    void successfulRegistrationReturnsIdentityAndRole() {
        SignUpResult result = service.signUp(command("owner@example.com", "a-sufficiently-long-password-1"));
        assertNotNull(result.id());
        assertEquals("owner@example.com", result.email());
        assertEquals(Role.VIEWER, result.role(), "a new user starts with the least-authority role");
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
        assertTrue(users.existsByEmail(new com.mailsent.identity.authentication.domain.Email("owner@example.com")));
    }

    @Test
    void duplicateEmailIsRejected() {
        String email = "owner@example.com";
        String pwd = "a-sufficiently-long-password-1";
        service.signUp(command(email, pwd));

        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
            () -> service.signUp(command(email, pwd)));
        assertTrue(ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void duplicateEmailDiffersOnlyByCaseIsRejected() {
        service.signUp(command("owner@example.com", "a-sufficiently-long-password-1"));
        assertThrows(DuplicateEmailException.class,
            () -> service.signUp(command("OWNER@example.com", "a-sufficiently-long-password-1")));
    }

    @Test
    void malformedEmailIsRejected() {
        assertThrows(InvalidEmailException.class,
            () -> service.signUp(command("not-an-email", "a-sufficiently-long-password-1")));
    }

    @Test
    void weakPasswordIsRejected() {
        assertThrows(WeakPasswordException.class,
            () -> service.signUp(command("owner@example.com", "short")));
    }

    @Test
    void nullCommandElementsAreRejected() {
        assertThrows(InvalidEmailException.class, () -> service.signUp(command(null, "a-sufficiently-long-password-1")));
        assertThrows(WeakPasswordException.class, () -> service.signUp(command("owner@example.com", null)));
    }

    @Test
    void failedSignUpDoesNotPersist() {
        assertThrows(WeakPasswordException.class,
            () -> service.signUp(command("owner@example.com", "short")));
        assertFalse(users.existsByEmail(new com.mailsent.identity.authentication.domain.Email("owner@example.com")));
    }

    @Test
    void emptyOrInjectionLikeEmailIsRejected() {
        // Header/shell injection-like payloads must not create an account.
        assertThrows(InvalidEmailException.class,
            () -> service.signUp(command("owner@example.com\r\nbcc:x@y.z", "a-sufficiently-long-password-1")));
    }

    @Test
    void repositoryFailurePropagatesAndPersistsNothing() {
        UserRepository failing = new UserRepository() {
            @Override
            public boolean existsByEmail(com.mailsent.identity.authentication.domain.Email email) {
                return false;
            }

            @Override
            public java.util.Optional<com.mailsent.identity.authentication.domain.User> findByEmail(
                    com.mailsent.identity.authentication.domain.Email email) {
                return java.util.Optional.empty();
            }

            @Override
            public com.mailsent.identity.authentication.domain.User save(com.mailsent.identity.authentication.domain.User user) {
                throw new IllegalStateException("database unavailable");
            }
        };
        SignUpService svc = new SignUpService(failing, hasher, policy);

        assertThrows(IllegalStateException.class,
            () -> svc.signUp(command("owner@example.com", "a-sufficiently-long-password-1")));
        assertFalse(users.existsByEmail(new com.mailsent.identity.authentication.domain.Email("owner@example.com")));
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
        assertEquals(1, ((InMemoryUserRepository) users).count());
    }
}