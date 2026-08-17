package com.mailsent.alerts.domain;

/**
 * AlertSeverity — ALERT-001 severity levels for risks (FUN-M5).
 *
 * <p>Classifies risks so a user can judge urgency. The canonical levels
 * reflect standard operational urgency tiers: CRITICAL (immediate action required),
 * WARNING (attention needed), and INFO (awareness).</p>
 */
public enum AlertSeverity {
    
    CRITICAL("critical"),
    WARNING("warning"),
    INFO("info");

    private final String token;

    AlertSeverity(String token) {
        this.token = token;
    }

    /** The persisted / wire-format token. */
    public String token() {
        return token;
    }

    /** Parses a stored token; unknown spellings are refused rather than coerced. */
    public static AlertSeverity fromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("An alert severity token is required.");
        }
        for (AlertSeverity severity : values()) {
            if (severity.token.equals(token)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown alert severity: " + token);
    }
}
