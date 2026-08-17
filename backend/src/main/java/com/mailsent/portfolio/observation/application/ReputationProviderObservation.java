package com.mailsent.portfolio.observation.application;

import java.time.Instant;

/**
 * A real observation returned by a reputation provider port. Absence of this
 * value (empty Optional) means the provider is unavailable or returned nothing
 * — there is then no fact to persist (INV-01).
 */
public final class ReputationProviderObservation {

    private final String payload;
    private final Instant observedAt;

    public ReputationProviderObservation(String payload, Instant observedAt) {
        if (payload == null) {
            throw new IllegalArgumentException("A raw provider payload is required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        this.payload = payload;
        this.observedAt = observedAt;
    }

    /** Opaque raw payload from the provider. */
    public String payload() {
        return payload;
    }

    /**
     * Instant supplied by the provider, or {@link Instant#now()} for this query
     * when the provider does not supply a time. Never a fabricated historical
     * timestamp.
     */
    public Instant observedAt() {
        return observedAt;
    }
}
