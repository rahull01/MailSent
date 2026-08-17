package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.List;

/**
 * DnsHealthFacts — the raw TTL, DNSSEC, and propagation facts of one DNS health
 * observation (INF-007).
 *
 * <p>This is not a Health Signal. TTL values are seconds as returned. DNSSEC is
 * presence/absence/unavailable of DNSKEY and DS, not cryptographic validation.
 * Propagation is per-resolver raw answers, not a percentage.</p>
 */
public final class DnsHealthFacts {

    private final Instant observedAt;
    private final List<TtlObservation> ttlObservations;
    private final List<DnssecObservation> dnssecObservations;
    private final List<PropagationObservation> propagationObservations;

    private DnsHealthFacts(Instant observedAt, List<TtlObservation> ttlObservations,
                           List<DnssecObservation> dnssecObservations,
                           List<PropagationObservation> propagationObservations) {
        this.observedAt = observedAt;
        this.ttlObservations = List.copyOf(ttlObservations);
        this.dnssecObservations = List.copyOf(dnssecObservations);
        this.propagationObservations = List.copyOf(propagationObservations);
    }

    public static DnsHealthFacts of(Instant observedAt, List<TtlObservation> ttlObservations,
                                    List<DnssecObservation> dnssecObservations,
                                    List<PropagationObservation> propagationObservations) {
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        if (ttlObservations == null || dnssecObservations == null || propagationObservations == null) {
            throw new IllegalArgumentException("TTL, DNSSEC, and propagation facts are required.");
        }
        return new DnsHealthFacts(observedAt, ttlObservations, dnssecObservations, propagationObservations);
    }

    public Instant observedAt() {
        return observedAt;
    }

    public List<TtlObservation> ttlObservations() {
        return ttlObservations;
    }

    public List<DnssecObservation> dnssecObservations() {
        return dnssecObservations;
    }

    public List<PropagationObservation> propagationObservations() {
        return propagationObservations;
    }

    /**
     * Whether this batch contains at least one real observation: a TTL answer,
     * a completed DNSSEC lookup (present or absent), or an available resolver
     * answer. Unavailable-only batches are not persisted.
     */
    public boolean hasRealObservation() {
        if (!ttlObservations.isEmpty()) {
            return true;
        }
        if (dnssecObservations.stream().anyMatch(d -> d.presence() != DnssecPresence.UNAVAILABLE)) {
            return true;
        }
        return propagationObservations.stream().anyMatch(PropagationObservation::available);
    }
}
