package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.observation.domain.DnsObservation;

/**
 * DnsResolver — the port for reading a domain's DNS records (INF-001).
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter (the dnsjava-backed
 * resolver in the non-test profile, or a deterministic test double in tests). An
 * implementation performs a REAL, bounded DNS read with a timeout and never fabricates
 * records (INV-01).</p>
 */
public interface DnsResolver {

    /**
     * Observes the DNS records of a domain.
     *
     * @param domainName the canonical (case-folded) domain name
     * @return the observed records
     * @throws DnsObservationException if the lookup fails (NXDOMAIN, timeout, I/O)
     */
    DnsObservation observe(String domainName);
}