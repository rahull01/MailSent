package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * DnsHealthObservation — an immutable persisted snapshot of one raw DNS health
 * observation (INF-007): TTL, DNSSEC-related records, and multi-resolver
 * propagation.
 *
 * <p>Owned by the Delivery Observation bounded context. It is not Health Signal,
 * not Trust Score, not HEALTH-001, and not Observation Evidence. It records
 * facts actually returned for a registered {@link Domain}. Tenant binding is
 * derived from that Domain (organization and domain identity never come from
 * the caller).</p>
 *
 * <p>Factory-based creation only ({@link #capture}, {@link #restore}). No setters,
 * no update, no delete. A later observation of the same domain is a new
 * snapshot.</p>
 */
public final class DnsHealthObservation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final Instant observedAt;
    private final List<TtlObservation> ttlObservations;
    private final List<DnssecObservation> dnssecObservations;
    private final List<PropagationObservation> propagationObservations;

    private DnsHealthObservation(String id, String organizationId, String domainId, String domainName,
                                 Instant observedAt, List<TtlObservation> ttlObservations,
                                 List<DnssecObservation> dnssecObservations,
                                 List<PropagationObservation> propagationObservations) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.observedAt = observedAt;
        this.ttlObservations = List.copyOf(ttlObservations);
        this.dnssecObservations = List.copyOf(dnssecObservations);
        this.propagationObservations = List.copyOf(propagationObservations);
    }

    /**
     * Captures one factual DNS-health observation against the registered domain.
     * Identity and tenant fields are taken from {@code domain}; TTL, DNSSEC,
     * and propagation facts from {@code facts}; {@code observedAt} from the
     * observation instant (never a second clock).
     */
    public static DnsHealthObservation capture(Domain domain, DnsHealthFacts facts, Instant observedAt) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (facts == null) {
            throw new IllegalArgumentException("DNS health facts are required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        return new DnsHealthObservation(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            observedAt,
            facts.ttlObservations(),
            facts.dnssecObservations(),
            facts.propagationObservations());
    }

    /**
     * Reconstructs DnsHealthObservation from durable storage, adopting the stored
     * identity, tenant binding, facts, and observation instant verbatim.
     */
    public static DnsHealthObservation restore(String id, String organizationId, String domainId,
                                               String domainName, Instant observedAt,
                                               List<TtlObservation> ttlObservations,
                                               List<DnssecObservation> dnssecObservations,
                                               List<PropagationObservation> propagationObservations) {
        return new DnsHealthObservation(id, organizationId, domainId, domainName, observedAt,
            ttlObservations, dnssecObservations, propagationObservations);
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

    /** The instant this observation was made. */
    public Instant observedAt() {
        return observedAt;
    }

    /** Raw per-record TTL facts. */
    public List<TtlObservation> ttlObservations() {
        return ttlObservations;
    }

    /** Raw DNSKEY/DS presence facts (not cryptographic validation). */
    public List<DnssecObservation> dnssecObservations() {
        return dnssecObservations;
    }

    /** Raw per-resolver answers (unavailable resolvers have empty answers). */
    public List<PropagationObservation> propagationObservations() {
        return propagationObservations;
    }
}
