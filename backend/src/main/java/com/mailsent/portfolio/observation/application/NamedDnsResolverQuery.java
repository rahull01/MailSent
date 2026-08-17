package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.observation.domain.PropagationObservation;

/**
 * NamedDnsResolverQuery — small port for querying one named DNS resolver
 * (INF-007 propagation).
 *
 * <p>Resolver identities are opaque strings such as {@code system} (the host
 * default resolver) or {@code 8.8.8.8} (a well-known public resolver, not a
 * paid vendor). A failed query must return
 * {@link PropagationObservation#unavailable} with no fabricated answers.</p>
 */
public interface NamedDnsResolverQuery {

    /**
     * Queries {@code resolverIdentity} for MX of {@code domainName} and returns
     * the raw answers or an unavailable observation.
     */
    PropagationObservation queryMx(String resolverIdentity, String domainName);
}
