package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.observation.domain.DnsHealthFacts;

/**
 * DnsHealthObserver — port for one raw DNS health observation (INF-007).
 *
 * <p>Returns TTL, DNSSEC presence, and propagation facts. Implementations must
 * not invent answers. If no real observation can be obtained, they throw
 * {@link DnsObservationException} and the application persists nothing.</p>
 */
public interface DnsHealthObserver {

    /**
     * Observes TTL, DNSSEC-related records, and named-resolver propagation for
     * {@code domainName}.
     *
     * @throws DnsObservationException if the observation cannot be obtained
     */
    DnsHealthFacts observe(String domainName);
}
