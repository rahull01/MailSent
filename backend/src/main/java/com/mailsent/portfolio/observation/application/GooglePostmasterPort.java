package com.mailsent.portfolio.observation.application;

import java.util.Optional;

/**
 * Port for reading a domain's Google Postmaster reputation observation.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. A real
 * observation is returned only when the provider actually supplies one. Empty
 * means unavailable or no observation — the application must not persist a
 * fabricated fact.</p>
 */
public interface GooglePostmasterPort {

    /**
     * Queries Postmaster for {@code domainName}.
     *
     * @return a real observation, or empty when the provider is unavailable
     */
    Optional<ReputationProviderObservation> query(String domainName);
}
