package com.mailsent.identity.authentication.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.InvalidCredentialsException;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.PasswordPolicy;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.InMemorySessionRepository;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.authentication.infrastructure.Pbkdf2PasswordHasher;
import com.mailsent.identity.authentication.infrastructure.SecureSessionTokenFactory;

/**
 * Unit tests for {@link SignInService} — the sign-in use case (AUTH-001).
 *
 * <p>Uses real hashing and hermetic in-memory repositories so the constant-time
 * verification and atomic session persistence are exercised for real.</p>
 */
class SignInServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);
    private static final String PWD = "a-sufficiently-long-password-1";

    private InMemoryUserRepository users;
    private InMemorySessionRepository sessions;
    private PasswordHasher hasher;
    private SignInService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        sessions = new InMemorySessionRepository();
        hasher = new Pbkdf2PasswordHasher();
        PasswordPolicy policy = new PasswordPolicy(12);
        service = new SignInService(users, sessions, hasher, new SecureSessionTokenFactory(), CLOCK,
            Duration.ofMinutes(60));
        // Seed a registered account so sign-in has something to authenticate.
        users.save(User.register(new Email("owner@example.com"), hasher.hash(PWD), CLOCK.instant()));
    }

    @Test
    void successfulSignInReturnsSessionAndIdentity() {
        SignInResult result = service.signIn(new SignInCommand("owner@example.com", PWD));

        assertEquals("owner@example.com", result.email());
        assertEquals("Viewer", result.role().label());
        assertNotEquals(result.token().rawToken(), result.token().tokenHash());
        assertEquals(1, sessions.count(), "a session is persisted atomically");
        // Raw token returned once; only its hash stored.
        assertTrue(users.findByEmail(new Email("owner@example.com")).isPresent());
    }

    @Test
    void wrongPasswordRejected() {
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
            () -> service.signIn(new SignInCommand("owner@example.com", "wrong-password-entirely")));
        assertTrue(ex.getMessage().toLowerCase().contains("email or password"));
        assertEquals(0, sessions.count(), "no session is created on failure");
    }

    @Test
    void unknownEmailRejectedWithSameError() {
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
            () -> service.signIn(new SignInCommand("nobody@example.com", PWD)));
        assertEquals("The email or password is incorrect.", ex.getMessage());
        assertEquals(0, sessions.count());
    }

    @Test
    void wrongPasswordAndUnknownEmailAreIndistinguishable() {
        // Anti-enumeration: same message whether email is unknown or password wrong.
        var unknown = assertThrows(InvalidCredentialsException.class,
            () -> service.signIn(new SignInCommand("nobody@example.com", PWD)));
        var wrongPwd = assertThrows(InvalidCredentialsException.class,
            () -> service.signIn(new SignInCommand("owner@example.com", "wrong-password-entirely")));
        assertEquals(unknown.getMessage(), wrongPwd.getMessage());
    }

    @Test
    void emailIsNormalizedForLookup() {
        SignInResult result = service.signIn(new SignInCommand("  OWNER@EXAMPLE.COM  ", PWD));
        assertEquals("owner@example.com", result.email());
    }

    @Test
    void sessionsAreDistinctPerSignIn() {
        SignInResult a = service.signIn(new SignInCommand("owner@example.com", PWD));
        SignInResult b = service.signIn(new SignInCommand("owner@example.com", PWD));
        assertNotEquals(a.sessionId(), b.sessionId());
        assertNotEquals(a.token().rawToken(), b.token().rawToken());
        assertEquals(2, sessions.count());
    }

    @Test
    void unknownEmailAndWrongPasswordPerformEqualHashChecks() {
        // Regression for a timing-equalization bug: both failure branches must perform
        // EXACTLY ONE password-hash verification. If one branch did more PBKDF2 work, an
        // attacker could distinguish "unknown email" from "wrong password" by timing.
        CountingHasher countingHasher = new CountingHasher(hasher);
        SignInService countingService = new SignInService(users, sessions, countingHasher,
            new SecureSessionTokenFactory(), CLOCK, Duration.ofMinutes(60));

        assertThrows(InvalidCredentialsException.class,
            () -> countingService.signIn(new SignInCommand("nobody@example.com", PWD)));
        int unknownEmailChecks = countingHasher.matchesCalls;
        countingHasher.matchesCalls = 0;

        assertThrows(InvalidCredentialsException.class,
            () -> countingService.signIn(new SignInCommand("owner@example.com",
                "wrong-password-entirely")));
        int wrongPasswordChecks = countingHasher.matchesCalls;

        assertEquals(1, unknownEmailChecks, "unknown-email branch must hash exactly once");
        assertEquals(1, wrongPasswordChecks, "wrong-password branch must hash exactly once");
    }

    /**
     * Minimal {@link PasswordHasher} wrapper that counts {@code matches} invocations so
     * tests can assert the amount of verification work performed, not just its result.
     */
    private static final class CountingHasher implements PasswordHasher {
        private final PasswordHasher delegate;
        private int matchesCalls;

        CountingHasher(PasswordHasher delegate) {
            this.delegate = delegate;
        }

        @Override
        public String hash(String rawPassword) {
            return delegate.hash(rawPassword);
        }

        @Override
        public boolean matches(String rawPassword, String storedHash) {
            matchesCalls++;
            return delegate.matches(rawPassword, storedHash);
        }
    }

    @Test
    void sessionExpiryIsBoundedByLifetime() {
        SignInResult result = service.signIn(new SignInCommand("owner@example.com", PWD));
        // Issued at 10:00:00Z with a 60-minute lifetime -> expires at 11:00:00Z.
        long expectedExpiry = CLOCK.instant().plus(Duration.ofMinutes(60)).getEpochSecond();
        assertEquals(expectedExpiry, result.expiresAtEpochSecond());
        assertTrue(result.expiresAtEpochSecond() > CLOCK.instant().getEpochSecond());
    }
}