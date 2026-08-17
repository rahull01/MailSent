package com.mailsent.portfolio.observation.infrastructure;

import java.util.Optional;

import com.mailsent.portfolio.observation.application.BlacklistProviderObservation;
import com.mailsent.portfolio.observation.application.UriblPort;

/**
 * Production URIBL adapter. No verified URIBL HTTP API, DNS zone,
 * credentials, or response schema exists in this repository, so this adapter
 * reports unavailability and never invents a blacklist fact.
 */
public class UnavailableUriblAdapter implements UriblPort {

    @Override
    public Optional<BlacklistProviderObservation> query(String domainName) {
        return Optional.empty();
    }
}
