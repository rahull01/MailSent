package com.mailsent.identity.authentication.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * User — a person acting within an organization (Glossary; Domain Model;
 * Data Model entity). An entity with a stable identity that is created at sign up.
 *
 * <p>The entity holds only what sign-up needs: a stable identity, the canonical
 * {@link Email}, the irreversible password hash (never the raw password), the role
 * assigned at creation, and the moment of registration. Further identity concerns
 * (consent records, role escalation, team membership) belong to later AUTH tasks in
 * the Identity and Access context.</p>
 */
public final class User {

    private final String id;
    private final Email email;
    private final String passwordHash;
    private final Role role;
    private final Instant createdAt;
    private final String organizationId;

    private User(String id, Email email, String passwordHash, Role role, Instant createdAt,
                 String organizationId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
        this.organizationId = organizationId;
    }

    /**
     * Creates a new User for sign up, assigning the least-authority default role.
     *
     * <p>The {@code organizationId} is required and mandatory (D6 / "a user belongs to
     * exactly one organization"): a signup always binds the user to its home Organization,
     * so there is never a User without one.</p>
     *
     * @param email          the canonical identity
     * @param passwordHash   the hashed password (never the raw password)
     * @param now            the registration instant
     * @param organizationId the home Organization the user belongs to (exactly one)
     */
    public static User register(Email email, String passwordHash, Instant now, String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("A user must belong to exactly one organization.");
        }
        return new User(UUID.randomUUID().toString(), email, passwordHash, Role.defaultForSignUp(), now,
            organizationId);
    }

    /**
     * Reconstructs a User from durable storage, preserving its assigned stable identity.
     *
     * <p>Used only by the persistence adapter to hydrate an aggregate that already has an
     * identity (INV-10 stable identity). It never reassigns the id or alters the role; it
     * is the inverse of persisting a {@link User}.</p>
     *
     * @param id             the stored stable identity
     * @param email          the canonical email
     * @param passwordHash   the stored irreversible hash
     * @param role           the stored role
     * @param createdAt      the stored registration instant
     * @param organizationId the stored home Organization id
     */
    public static User restore(String id, Email email, String passwordHash, Role role, Instant createdAt,
                               String organizationId) {
        return new User(id, email, passwordHash, role, createdAt, organizationId);
    }

    /** The stable identity of the user. */
    public String id() {
        return id;
    }

    /** The canonical email identity. */
    public Email email() {
        return email;
    }

    /** The irreversible password hash; the raw password is never retained. */
    public String passwordHash() {
        return passwordHash;
    }

    /** The role assigned at creation (least authority). */
    public Role role() {
        return role;
    }

    /** The instant of registration. */
    public Instant createdAt() {
        return createdAt;
    }

    /** The home Organization the user belongs to (exactly one, INV-09 / D6). */
    public String organizationId() {
        return organizationId;
    }
}