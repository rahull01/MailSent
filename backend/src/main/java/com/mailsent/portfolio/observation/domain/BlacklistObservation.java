package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * BlacklistObservation — an immutable persisted snapshot of one raw external
 * blacklist observation (INF-008).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not a Health
 * Signal: it records the opaque payload actually returned by Spamhaus,
 * Barracuda, SURBL, or URIBL for a registered {@link Domain}. Tenant binding
 * is derived from that Domain (organization and domain identity never come
 * from the caller).</p>
 *
 * <p>Factory-based creation only ({@link #capture}, {@link #restore}). No setters,
 * no update, no delete. A later observation of the same domain is a new
 * snapshot.</p>
 */
public final class BlacklistObservation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final BlacklistSource source;
    private final Instant observedAt;
    private final String payload;

    private BlacklistObservation(String id, String organizationId, String domainId, String domainName,
                                 BlacklistSource source, Instant observedAt, String payload) {
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
    public static BlacklistObservation capture(Domain domain, BlacklistSource source, String payload,
                                               Instant observedAt) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (source == null) {
            throw new IllegalArgumentException("A blacklist source is required.");
        }
        if (payload == null) {
            throw new IllegalArgumentException("A raw provider payload is required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        return new BlacklistObservation(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            source,
            observedAt,
            payload);
    }

    /**
     * Reconstructs BlacklistObservation from durable storage, adopting the stored
     * identity, tenant binding, source, payload, and observation instant verbatim.
     */
    public static BlacklistObservation restore(String id, String organizationId, String domainId,
                                               String domainName, BlacklistSource source,
                                               Instant observedAt, String payload) {
        return new BlacklistObservation(id, organizationId, domainId, domainName, source, observedAt, payload);
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

    /**
     * The provider that produced this observation ({@code SPAMHAUS},
     * {@code BARRACUDA}, {@code SURBL}, or {@code URIBL}).
     */
    public BlacklistSource source() {
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
