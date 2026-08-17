package com.mailsent.portfolio.observation.domain;

/**
 * DnssecPresence — raw presence of DNSSEC-related records (INF-007).
 *
 * <p>{@link #PRESENT} and {@link #ABSENT} are successful lookups. {@link #UNAVAILABLE}
 * is an observation error. This is not cryptographic validation and is not
 * "DNSSEC valid".</p>
 */
public enum DnssecPresence {
    PRESENT,
    ABSENT,
    UNAVAILABLE
}
