package com.mailsent.portfolio.observation.application;

/**
 * Bounded rate/volume guard for INT-003 receive. Implementations must treat
 * known and unknown uuid identically.
 */
public interface DmarcIngestRateLimiter {

    /**
     * @return {@code true} when the request may proceed; {@code false} when it
     *         must be rejected with HTTP 429 and not persisted
     */
    boolean allow(String clientIp, String uuid);
}
