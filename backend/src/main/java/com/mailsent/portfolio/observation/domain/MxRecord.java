package com.mailsent.portfolio.observation.domain;

/**
 * MxRecord — one observed MX record of a domain (INF-001).
 *
 * <p>An immutable, deterministic value: the preference (priority) and the mail exchange
 * hostname. Exchanges are compared lower-cased and trailing-dot-free, matching DNS.</p>
 *
 * @param priority the MX preference (lower is preferred)
 * @param exchange the mail exchange hostname
 */
public record MxRecord(int priority, String exchange) {

    public MxRecord {
        if (priority < 0) {
            throw new IllegalArgumentException("An MX priority cannot be negative.");
        }
        if (exchange == null || exchange.isBlank()) {
            throw new IllegalArgumentException("An MX exchange hostname is required.");
        }
    }
}