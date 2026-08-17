package com.mailsent.portfolio.observation.application;

import java.util.HashMap;
import java.util.Map;

/**
 * Test double for {@link SmtpProbe}. Never opens a network socket.
 * Unconfigured IPs are unavailable, not fabricated greetings.
 */
public final class FakeSmtpProbe implements SmtpProbe {

    public int probeCount;

    private final Map<String, SmtpProbeResult> byIp = new HashMap<>();
    private final Map<String, Boolean> fail = new HashMap<>();

    public void succeed(String ip, String rawGreeting, String rawEhlo) {
        byIp.put(ip, SmtpProbeResult.connected(rawGreeting, rawEhlo));
    }

    public void refused(String ip) {
        byIp.put(ip, SmtpProbeResult.refused());
    }

    public void timeout(String ip) {
        byIp.put(ip, SmtpProbeResult.timeout());
    }

    public void unavailable(String ip) {
        byIp.put(ip, SmtpProbeResult.unavailable());
    }

    public void fail(String ip) {
        fail.put(ip, true);
    }

    @Override
    public SmtpProbeResult probe(String ip, String ehloDomain) {
        probeCount++;
        if (Boolean.TRUE.equals(fail.get(ip))) {
            throw new RuntimeException("SMTP probe failed for " + ip);
        }
        SmtpProbeResult configured = byIp.get(ip);
        return configured == null ? SmtpProbeResult.unavailable() : configured;
    }
}
