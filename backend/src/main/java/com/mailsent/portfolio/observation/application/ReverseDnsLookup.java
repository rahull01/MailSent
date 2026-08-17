package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

/**
 * ReverseDnsLookup — application port for PTR resolution of an IP address
 * (INF-005). Separate from {@code DnsResolver.observe} (INF-001).
 *
 * <p>{@link Optional#empty()} means the PTR lookup was unavailable (timeout,
 * UNRECOVERABLE, TRY_AGAIN). A present list — including empty — is a completed
 * successful lookup (NODATA is a real empty PTR list). Do not fabricate PTR
 * names. Do not treat unavailable as a successful empty list.</p>
 */
public interface ReverseDnsLookup {

    /**
     * Looks up PTR records for an IP address.
     *
     * @param ipAddress the IPv4 or IPv6 address as observed
     * @return empty if unavailable; a (possibly empty) list of PTR names if successful
     */
    Optional<List<String>> lookupPtr(String ipAddress);
}
