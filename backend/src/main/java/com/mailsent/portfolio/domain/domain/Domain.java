package com.mailsent.portfolio.domain.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Domain — a real-world sending domain (e.g. {@code mail.example.com}) that belongs to
 * exactly one {@code Organization} (Domain Model §3; Data Model §3).
 *
 * <p>ORG-003 establishes domain ownership. A domain name is globally unique: a real-world
 * domain can be registered by exactly one Organization, which the database {@code UNIQUE}
 * constraint on {@code domains.name} enforces as the final integrity backstop. The
 * {@code organization_id} is {@code NOT NULL} with a foreign key to {@code organizations},
 * so a domain can never exist without (or reference a different) owning organization.</p>
 *
 * <p><strong>Scope.</strong> The identity/name/ownership-and-registration surface only.
 * DNS observation, sender resolution, reassignment, domain health checks, and internal
 * dynamic-sender mappings are later ORG/AUTH tasks and are deliberately absent.</p>
 */
public final class Domain {

    private final String id;
    private final String organizationId;
    private final String name;
    private final Instant createdAt;

    private Domain(String id, String organizationId, String name, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.createdAt = createdAt;
    }

    /**
     * Registers a domain under a single owning organization. Folds the name to lowercase
     * (root locale) so uniqueness is case-insensitive, consistent with how email is
     * folded in the identity context.
     *
     * @param organizationId the stable id of the single owning organization, required
     * @param name           the domain name, required and non-blank
     * @param createdAt      the registration instant, required
     */
    public static Domain register(String organizationId, String name, Instant createdAt) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("An owning organization id is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A domain name is required.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("A registration time is required.");
        }
        return new Domain(UUID.randomUUID().toString(), organizationId, normalize(name), createdAt);
    }

    /**
     * Reconstructs a Domain from durable storage, adopting the stored identity, owner,
     * name, and registration instant verbatim (no re-normalization, no reassignment).
     */
    public static Domain restore(String id, String organizationId, String name, Instant createdAt) {
        return new Domain(id, organizationId, name, createdAt);
    }

    /** Case-folds a domain name to its canonical, globally-comparable form. */
    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    /** The stable identity of the domain. */
    public String id() {
        return id;
    }

    /** The stable identity of the single owning organization (INV-09). */
    public String organizationId() {
        return organizationId;
    }

    /** The canonical (case-folded) domain name. */
    public String name() {
        return name;
    }

    /** The instant the domain was registered to its organization. */
    public Instant createdAt() {
        return createdAt;
    }
}