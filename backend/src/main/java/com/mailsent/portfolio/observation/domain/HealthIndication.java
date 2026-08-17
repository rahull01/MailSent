package com.mailsent.portfolio.observation.domain;

/**
 * HealthIndication — the unified Health Signal vocabulary (HEALTH-001).
 *
 * <p>Canonical tokens: {@code healthy}, {@code at-risk}, {@code unknown}.
 * HEALTH-001 assigns these from cited V15 judgment tokens when required
 * snapshots exist.</p>
 */
public enum HealthIndication {

    HEALTHY("healthy"),
    AT_RISK("at-risk"),
    UNKNOWN("unknown");

    private final String token;

    HealthIndication(String token) {
        this.token = token;
    }

    /** The persisted / product token. */
    public String token() {
        return token;
    }

    /** Parses a stored token; unknown spellings are refused rather than coerced. */
    public static HealthIndication fromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("A health indication is required.");
        }
        for (HealthIndication indication : values()) {
            if (indication.token.equals(token)) {
                return indication;
            }
        }
        throw new IllegalArgumentException("Unknown health indication: " + token);
    }
}
