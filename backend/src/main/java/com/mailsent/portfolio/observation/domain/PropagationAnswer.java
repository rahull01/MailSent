package com.mailsent.portfolio.observation.domain;

/**
 * PropagationAnswer — one raw answer from a named resolver (INF-007).
 *
 * <p>Rdata and TTL are stored as observed. This is not a propagation percentage
 * or health conclusion.</p>
 */
public record PropagationAnswer(String rdata, long ttlSeconds) {

    public PropagationAnswer {
        if (rdata == null) {
            throw new IllegalArgumentException("Propagation rdata is required (empty string if none).");
        }
        if (ttlSeconds < 0) {
            throw new IllegalArgumentException("A TTL cannot be negative.");
        }
    }
}
