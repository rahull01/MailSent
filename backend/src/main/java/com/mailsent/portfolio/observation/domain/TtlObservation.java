package com.mailsent.portfolio.observation.domain;

/**
 * TtlObservation — one raw per-record TTL fact (INF-007).
 *
 * <p>TTL seconds are stored as observed. This is not a health, optimal, or
 * warning conclusion.</p>
 */
public record TtlObservation(String recordType, String owner, String rdata, long ttlSeconds) {

    public TtlObservation {
        if (recordType == null || recordType.isBlank()) {
            throw new IllegalArgumentException("A TTL record type is required.");
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("A TTL owner name is required.");
        }
        if (rdata == null) {
            throw new IllegalArgumentException("TTL rdata is required (empty string if none).");
        }
        if (ttlSeconds < 0) {
            throw new IllegalArgumentException("A TTL cannot be negative.");
        }
    }
}
