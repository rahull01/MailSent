package com.mailsent.portfolio.observation.domain;

/**
 * DnssecRecordFact — one raw DNSKEY or DS answer (INF-007).
 *
 * <p>Owner and rdata are stored as observed strings. This is not a validity
 * claim.</p>
 */
public record DnssecRecordFact(String owner, String rdata) {

    public DnssecRecordFact {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("A DNSSEC record owner is required.");
        }
        if (rdata == null) {
            throw new IllegalArgumentException("DNSSEC rdata is required (empty string if none).");
        }
    }
}
