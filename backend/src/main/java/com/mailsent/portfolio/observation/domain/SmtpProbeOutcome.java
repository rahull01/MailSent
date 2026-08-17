package com.mailsent.portfolio.observation.domain;

/**
 * SmtpProbeOutcome — the raw SMTP connection/probe outcome as observed
 * (INF-006). Not a health, validity, deliverability, or open-relay conclusion.
 */
public enum SmtpProbeOutcome {
    CONNECTED,
    UNAVAILABLE,
    REFUSED,
    TIMEOUT
}
