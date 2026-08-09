package com.mailsent.identity.authentication.application;

import java.time.Instant;

import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.InvalidEmailException;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.PasswordPolicy;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.domain.WeakPasswordException;

/**
 * SignUpService — the sign-up use case (AUTH-000).
 *
 * <p>Orchestrates the business rules in order, as the application layer coordinates
 * domain behavior:</p>
 * <ol>
 *   <li>Validate the offered email (domain {@link Email}).</li>
 *   <li>Validate the offered password against the {@link PasswordPolicy}.</li>
 *   <li>Fast-path duplicate check (see race-safety note below).</li>
 *   <li>Hash the password via the {@link PasswordHasher} — never stored in plain text.</li>
 *   <li>Create a {@link User} with the least-authority default role.</li>
 *   <li>Persist atomically through the {@link UserRepository}.</li>
 * </ol>
 *
 * <p><strong>Race safety.</strong> The {@code existsByEmail} pre-check is a fast-path
 * that fails quickly on the common duplicate. It is <em>not</em> the authoritative
 * guard: under concurrency the repository enforces email uniqueness atomically (the
 * production adapter relies on the {@code uk_users_email} unique constraint and surfaces
 * a constraint violation as {@link DuplicateEmailException}). This removes the
 * check-then-insert race so two concurrent sign-ups cannot both succeed for one email
 * (INV-10).</p>
 *
 * <p><strong>Idempotency.</strong> Sign-up is intentionally <em>not</em> idempotent: a
 * repeat of an already-registered email returns the duplicate error rather than
 * silently succeeding, so no second account is ever created for the same identity.</p>
 *
 * <p><strong>Single responsibility.</strong> This class validates and orchestrates only;
 * password hashing, uniqueness, and persistence are delegated to their own ports/adapters,
 * keeping the use case independent of storage and crypto technology.</p>
 *
 * <p>Failures raise a specific domain exception that the interfaces layer maps to a
 * business-first error.</p>
 */
public class SignUpService {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;

    public SignUpService(UserRepository users, PasswordHasher passwordHasher, PasswordPolicy passwordPolicy) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
    }

    /**
     * Registers a new user.
     *
     * @param command the offered email and password
     * @return the {@link SignUpResult} describing the created identity and role
     * @throws InvalidEmailException    when the email is missing/malformed
     * @throws WeakPasswordException    when the password fails the policy
     * @throws DuplicateEmailException  when the email is already registered
     */
    public SignUpResult signUp(SignUpCommand command) {
        Email email = new Email(command.email());
        passwordPolicy.validate(command.password());

        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException(
                "An account already exists for that email address.");
        }

        String hash = passwordHasher.hash(command.password());
        User user = User.register(email, hash, Instant.now());
        User saved = users.save(user);

        return new SignUpResult(saved.id(), saved.email().value(), saved.role());
    }
}