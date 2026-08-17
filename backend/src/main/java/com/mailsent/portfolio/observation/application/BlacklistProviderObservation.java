package com.mailsent.portfolio.observation.application;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.BlacklistSource;

/**
 * A real observation returned by a blacklist provider port. Absence of this
 * value (empty Optional) means the provider is unavailable or returned nothing
 * — there is then no fact to persist (INV-01).
 */
public final class BlacklistProviderObservation {

    private final BlacklistSource source;
    private final Instant observedAt;
    private final String payload;

    public BlacklistProviderObservation(BlacklistSource source, Instant observedAt, String payload) {
        if (source == null) {
            throw new IllegalArgumentException("A blacklist source is required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        if (payload == null) {
            throw new IllegalArgumentException("A raw provider payload is required.");
        }
        this.source = source;
        this.observedAt = observedAt;
        this.payload = payload;
    }

    /** The provider that produced this observation. */
    public BlacklistSource source() {
        return source;
    }

    /**
     * Instant supplied by the provider, or {@link Instant#now()} for this query
     * when the provider does not supply a time. Never a fabricated historical
     * timestamp.
     */
    public Instant observedAt() {
        return observedAt;
    }

    /** Opaque raw payload from the provider. */
    public String payload() {
        return payload;
    }
}
