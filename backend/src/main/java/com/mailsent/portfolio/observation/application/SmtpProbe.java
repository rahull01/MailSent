package com.mailsent.portfolio.observation.application;

/**
 * SmtpProbe — application port for a bounded SMTP greeting/EHLO read of one IP
 * (INF-006). Separate from mail sending and from Observation Evidence.
 *
 * <p>The probe connects to port 25 of an IP that was resolved from an observed
 * MX hostname of a registered Domain. It may send EHLO (using the registered
 * domain name as the EHLO argument — protocol identity, not credentials) and
 * QUIT. It must not send MAIL FROM, AUTH, RCPT, DATA, or otherwise test relay
 * or delivery.</p>
 */
public interface SmtpProbe {

    /**
     * Observes SMTP at {@code ip}:25.
     *
     * @param ip         the resolved MX host address
     * @param ehloDomain the registered domain name used as the EHLO argument
     * @return the raw probe outcome and any greeting/EHLO text actually observed
     */
    SmtpProbeResult probe(String ip, String ehloDomain);
}
