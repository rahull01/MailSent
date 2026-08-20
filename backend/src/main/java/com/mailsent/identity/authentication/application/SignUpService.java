package com.mailsent.identity.authentication.application;

import java.time.Instant;

import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.InvalidEmailException;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.PasswordPolicy;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.domain.WeakPasswordException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.domain.Organization;

import org.springframework.transaction.annotation.Transactional;

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
    private final OrganizationRepository organizations;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final com.mailsent.identity.consent.application.ConsentRepository consentRepository;

    public SignUpService(UserRepository users, OrganizationRepository organizations,
                         PasswordHasher passwordHasher, PasswordPolicy passwordPolicy,
                         com.mailsent.identity.consent.application.ConsentRepository consentRepository) {
        this.users = users;
        this.organizations = organizations;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.consentRepository = consentRepository;
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
    /**
     * Registers a new user and its exactly-one home Organization, atomically.
     *
     * <p><strong>Transaction boundary (ORG-002).</strong> The method is one atomic
     * transaction: the Organization save and the User save both participate in it via the
     * default {@code REQUIRED} propagation. A success commits both; any failure (including
     * the race-safe duplicate-email constraint surfacing as {@link DuplicateEmailException})
     * rolls both back. There is never a successfully-created User without its Organization,
     * and never an orphan Organization from a failed signup.</p>
     *
     * @param command the offered email and password
     * @return the {@link SignUpResult} describing the created identity, role, and home Organization
     * @throws InvalidEmailException    when the email is missing/malformed
     * @throws WeakPasswordException    when the password fails the policy
     * @throws DuplicateEmailException  when the email is already registered
     */
    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        Email email = new Email(command.email());
        passwordPolicy.validate(command.password());

        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException(
                "An account already exists for that email address.");
        }

        String hash = passwordHasher.hash(command.password());

        // ORG-002: a successful signup creates exactly one Organization and binds the
        // signing User to it, committed together in this single transaction.
        Organization organization = Organization.create(provisionalOrganizationName(email), Instant.now());
        Organization savedOrganization = organizations.save(organization);

        User user = User.register(email, hash, Instant.now(), savedOrganization.id());
        User saved = users.save(user);

        // AUTH-000 / INV-02: Record initial consent.
        // We grant consent at the organization level ("*") since the domain is not yet created.
        com.mailsent.identity.consent.domain.Consent consent = com.mailsent.identity.consent.domain.Consent.grant(
            savedOrganization.id(),
            "*",
            saved.id(),
            com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE
        );
        consentRepository.save(consent);

        // The home Organization id stays internal (backend domain/persistence); it is not
        // part of the public sign-up contract (ORG-002 decision).
        return new SignUpResult(saved.id(), saved.email().value(), saved.role());
    }

    /**
     * A provisional, non-blank Organization name for the onboarding Organization,
     * derived from the owner's canonical email. The Organization requires a name at
     * creation (ORG-001); this is the unambiguous owning identity. Renaming and a true
     * business name are a later ORG-003 concern, not ORG-002.
     */
    private String provisionalOrganizationName(Email email) {
        String value = email.value().trim();
        // Keep comfortably under the organizations.name column width (VARCHAR(200)).
        return value.length() <= 190 ? value : value.substring(0, 190);
    }
}