package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;

/**
 * SmtpProbeResult — facts returned by one SMTP probe of a single IP (INF-006).
 * Greeting and EHLO text are present only when actually observed.
 */
public final class SmtpProbeResult {

    private final SmtpProbeOutcome outcome;
    private final String rawGreeting;
    private final String rawEhlo;

    private SmtpProbeResult(SmtpProbeOutcome outcome, String rawGreeting, String rawEhlo) {
        this.outcome = outcome;
        this.rawGreeting = rawGreeting;
        this.rawEhlo = rawEhlo;
    }

    /** Connected; greeting and EHLO may be empty strings when observed empty. */
    public static SmtpProbeResult connected(String rawGreeting, String rawEhlo) {
        return new SmtpProbeResult(SmtpProbeOutcome.CONNECTED, rawGreeting, rawEhlo);
    }

    public static SmtpProbeResult refused() {
        return new SmtpProbeResult(SmtpProbeOutcome.REFUSED, null, null);
    }

    public static SmtpProbeResult timeout() {
        return new SmtpProbeResult(SmtpProbeOutcome.TIMEOUT, null, null);
    }

    public static SmtpProbeResult unavailable() {
        return new SmtpProbeResult(SmtpProbeOutcome.UNAVAILABLE, null, null);
    }

    public SmtpProbeOutcome outcome() {
        return outcome;
    }

    public String rawGreeting() {
        return rawGreeting;
    }

    public String rawEhlo() {
        return rawEhlo;
    }
}
