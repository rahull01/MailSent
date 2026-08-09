package com.mailsent.identity.authentication.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.InvalidCredentialsException;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.Session;
import com.mailsent.identity.authentication.domain.SessionToken;
import com.mailsent.identity.authentication.domain.SessionTokenFactory;
import com.mailsent.identity.authentication.domain.User;

/**
 * SignInService — the sign-in use case (AUTH-001).
 *
 * <p>Authenticates an offered email and password and, on success, atomically establishes
 * a hardened {@link Session}. Orchestrates, in order:</p>
 * <ol>
 *   <li>Load the user by canonical email.</li>
 *   <li>Verify the password against the stored hash in constant time
 *       ({@link PasswordHasher#matches}).</li>
 *   <li>On any failure raise a single {@link InvalidCredentialsException} whose message
 *       does not reveal whether the email exists or only the password was wrong
 *       (anti-enumeration).</li>
 *   <li>On success, issue a high-entropy {@link SessionToken} and persist the session
 *       atomically (only the token hash is stored).</li>
 * </ol>
 *
 * <p><strong>Timing equalization.</strong> When the email is unknown, the use case still
 * runs a hash check against a fixed dummy hash so that "unknown email" and "wrong
 * password" cost comparable wall-clock time — preventing timing-based account
 * enumeration even when the observable response is identical.</p>
 */
public class SignInService {

    private final UserRepository users;
    private final SessionRepository sessions;
    private final PasswordHasher passwordHasher;
    private final SessionTokenFactory sessionTokenFactory;
    private final Clock clock;
    private final Duration sessionLifetime;
    /** A real hash used only to equalize timing for unknown-email attempts. */
    private final String dummyHash;

    public SignInService(UserRepository users, SessionRepository sessions, PasswordHasher passwordHasher,
                         SessionTokenFactory sessionTokenFactory, Clock clock, Duration sessionLifetime) {
        this.users = users;
        this.sessions = sessions;
        this.passwordHasher = passwordHasher;
        this.sessionTokenFactory = sessionTokenFactory;
        this.clock = clock;
        this.sessionLifetime = sessionLifetime;
        this.dummyHash = passwordHasher.hash("sign-in-timing-equalizer");
    }

    /**
     * Authenticates credentials and issues a session.
     *
     * @param command the offered email and password
     * @return the {@link SignInResult} with a fresh, one-time raw token
     * @throws InvalidCredentialsException when the email is unknown or the password is wrong
     */
    public SignInResult signIn(SignInCommand command) {
        String password = command.password();
        var user = users.findByEmail(new Email(command.email()));

        // Anti-enumeration + timing equalization: exactly ONE hash verification is
        // performed whether the email is unknown or the password is wrong. When the
        // account does not exist we verify against a fixed dummy hash, so both branches
        // cost equivalent work; an attacker cannot distinguish them by observing
        // response time or error body.
        String targetHash = user.isPresent() ? user.get().passwordHash() : dummyHash;
        boolean verified = passwordHasher.matches(password, targetHash);
        if (user.isEmpty() || !verified) {
            throw new InvalidCredentialsException("The email or password is incorrect.");
        }

        Instant now = Instant.now(clock);
        SessionToken token = sessionTokenFactory.randomToken();
        User account = user.orElseThrow();
        Session session = Session.issue(account.id(), token, now, sessionLifetime);
        Session saved = sessions.save(session);

        return new SignInResult(
            saved.id(), saved.userId(), account.email().value(), account.role(), token, saved.expiresAt().getEpochSecond());
    }
}