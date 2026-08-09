package com.mailsent.portfolio.organization.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Organization — a distinct business entity that owns domains and is served as a unit
 * (Domain Model §3; Data Model §3).
 *
 * <p>The Organization is the ownership and isolation boundary of the business
 * (INV-09: one organization's health and data never mix with another's). It holds a
 * stable identity (INV-10), a formation lifecycle, and a closure state — it "ends when
 * the customer closes it" (Data Model §3), which is a lifecycle closure, not a physical
 * deletion.</p>
 *
 * <p><strong>Scope.</strong> Membership, domain ownership, consent, and cross-organization
 * authorization are later ORG/AUTH tasks and are deliberately absent. Only the minimal
 * source-grounded fields are present: a stable identity, a {@code name} as the
 * discriminator for create/read/list, the formation instant, and lifecycle state. No
 * slug, owner, plan, subscription, logo, or other speculative fields are introduced
 * (scope discipline).</p>
 */
public final class Organization {

    /** Lifecycle state (Data Model §3: formed → persisted → closed). */
    public enum Status {
        ACTIVE,
        CLOSED
    }

    private final String id;
    private final String name;
    private final Instant createdAt;
    private Status status;
    private Instant closedAt;

    private Organization(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.status = Status.ACTIVE;
    }

    /**
     * Creates an Organization in its ACTIVE lifecycle state.
     *
     * @param name      the business name, required and non-blank
     * @param createdAt the formation instant, required
     */
    public static Organization create(String name, Instant createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("An organization name is required.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("A formation time is required.");
        }
        return new Organization(UUID.randomUUID().toString(), name.trim(), createdAt);
    }

    /**
     * Reconstructs an Organization from durable storage. Adopts the stored identity and
     * lifecycle state without reassigning them.
     *
     * @param id        the stored stable identity
     * @param name      the stored name
     * @param createdAt the stored formation instant
     * @param status    the stored lifecycle state
     * @param closedAt  the stored closure instant, or {@code null} if ACTIVE
     */
    public static Organization restore(String id, String name, Instant createdAt,
                                       Status status, Instant closedAt) {
        Organization o = new Organization(id, name, createdAt);
        o.status = status;
        o.closedAt = closedAt;
        return o;
    }

    /**
     * Closes the Organization, ending its lifecycle. A closed organization cannot be
     * reopened; closing an already-closed organization is a no-op.
     *
     * @param closedAt the closure instant, required
     */
    public void close(Instant closedAt) {
        if (closedAt == null) {
            throw new IllegalArgumentException("A closure time is required.");
        }
        if (status == Status.ACTIVE) {
            this.status = Status.CLOSED;
            this.closedAt = closedAt;
        }
    }

    /** The stable identity of the organization (INV-09 / INV-10). */
    public String id() {
        return id;
    }

    /** The business discriminator used for create/read/list. */
    public String name() {
        return name;
    }

    /** The instant the organization was formed. */
    public Instant createdAt() {
        return createdAt;
    }

    /** The current lifecycle state (ACTIVE or CLOSED). */
    public Status status() {
        return status;
    }

    /** The instant of closure, or {@code null} while ACTIVE. */
    public Instant closedAt() {
        return closedAt;
    }
}