package com.mailsent.portfolio.observation.application;

import java.util.Optional;

/**
 * Port for reading a domain's Spamhaus blacklist observation.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. A real
 * observation is returned only when the provider actually supplies one. Empty
 * means unavailable or no observation — the application must not persist a
 * fabricated fact.</p>
 */
public interface SpamhausPort {

    /**
     * Queries Spamhaus for {@code domainName}.
     *
     * @return a real observation, or empty when the provider is unavailable
     */
    Optional<BlacklistProviderObservation> query(String domainName);
}
