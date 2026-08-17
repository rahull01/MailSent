package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * ReputationObservation — an immutable persisted snapshot of one raw external
 * reputation observation (INF-003).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not a Reputation
 * aggregate and not Health Signal: it records the opaque payload actually
 * returned by Google Postmaster or Microsoft SNDS for a registered
 * {@link Domain}. Tenant binding is derived from that Domain (organization and
 * domain identity never come from the caller).</p>
 *
 * <p>Factory-based creation only ({@link #capture}, {@link #restore}). No setters,
 * no update, no delete. A later observation of the same domain is a new
 * snapshot.</p>
 */
public final class ReputationObservation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final ReputationSource source;
    private final Instant observedAt;
    private final String payload;

    private ReputationObservation(String id, String organizationId, String domainId, String domainName,
                                  ReputationSource source, Instant observedAt, String payload) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.source = source;
        this.observedAt = observedAt;
        this.payload = payload;
    }

    /**
     * Captures one factual provider observation against the registered domain
     * that was observed. Identity and tenant fields are taken from {@code domain}.
     */
    public static ReputationObservation capture(Domain domain, ReputationSource source, String payload,
                                                Instant observedAt) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (source == null) {
            throw new IllegalArgumentException("A reputation source is required.");
        }
        if (payload == null) {
            throw new IllegalArgumentException("A raw provider payload is required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        return new ReputationObservation(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            source,
            observedAt,
            payload);
    }

    /**
     * Reconstructs ReputationObservation from durable storage, adopting the stored
     * identity, tenant binding, source, payload, and observation instant verbatim.
     */
    public static ReputationObservation restore(String id, String organizationId, String domainId,
                                                String domainName, ReputationSource source,
                                                Instant observedAt, String payload) {
        return new ReputationObservation(id, organizationId, domainId, domainName, source, observedAt, payload);
    }

    /** The stable identity of this snapshot. */
    public String id() {
        return id;
    }

    /** The owning organization, derived from the registered Domain. */
    public String organizationId() {
        return organizationId;
    }

    /** The registered Domain's stable identity. */
    public String domainId() {
        return domainId;
    }

    /** The registered Domain's canonical name. */
    public String domainName() {
        return domainName;
    }

    /** The provider that produced this observation ({@code POSTMASTER} or {@code SNDS}). */
    public ReputationSource source() {
        return source;
    }

    /** The instant this observation was made (from the provider, or the query instant). */
    public Instant observedAt() {
        return observedAt;
    }

    /** The opaque raw provider payload, stored exactly as returned. */
    public String payload() {
        return payload;
    }
}
