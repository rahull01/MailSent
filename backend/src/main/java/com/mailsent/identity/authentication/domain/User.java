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

    private User(String id, Email email, String passwordHash, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new User for sign up, assigning the least-authority default role.
     *
     * @param email        the canonical identity
     * @param passwordHash the hashed password (never the raw password)
     * @param now          the registration instant
     */
    public static User register(Email email, String passwordHash, Instant now) {
        return new User(UUID.randomUUID().toString(), email, passwordHash, Role.defaultForSignUp(), now);
    }

    /**
     * Reconstructs a User from durable storage, preserving its assigned stable identity.
     *
     * <p>Used only by the persistence adapter to hydrate an aggregate that already has an
     * identity (INV-10 stable identity). It never reassigns the id or alters the role; it
     * is the inverse of persisting a {@link User}.</p>
     *
     * @param id           the stored stable identity
     * @param email        the canonical email
     * @param passwordHash the stored irreversible hash
     * @param role         the stored role
     * @param createdAt    the stored registration instant
     */
    public static User restore(String id, Email email, String passwordHash, Role role, Instant createdAt) {
        return new User(id, email, passwordHash, role, createdAt);
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
}