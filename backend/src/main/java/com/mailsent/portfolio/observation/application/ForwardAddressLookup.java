package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.IpAddressFamily;

/**
 * ForwardAddressLookup — application port for A/AAAA resolution of an MX hostname
 * (INF-005). Separate from {@code DnsResolver.observe} (INF-001).
 *
 * <p>{@link Optional#empty()} means the lookup was unavailable (timeout,
 * UNRECOVERABLE, TRY_AGAIN). A present list — including empty — is a completed
 * successful lookup. Empty successful is not failure and must not be treated as
 * unavailable.</p>
 */
public interface ForwardAddressLookup {

    /**
     * Resolves addresses of one family for a hostname.
     *
     * @param hostname the MX hostname to resolve
     * @param family   {@code IPV4} (A) or {@code IPV6} (AAAA)
     * @return empty if unavailable; a (possibly empty) list if the lookup succeeded
     */
    Optional<List<String>> lookup(String hostname, IpAddressFamily family);
}
