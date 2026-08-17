package com.mailsent.portfolio.observation.infrastructure;

import java.util.Optional;

import com.mailsent.portfolio.observation.application.BlacklistProviderObservation;
import com.mailsent.portfolio.observation.application.SurblPort;

/**
 * Production SURBL adapter. No verified SURBL HTTP API, DNS zone,
 * credentials, or response schema exists in this repository, so this adapter
 * reports unavailability and never invents a blacklist fact.
 */
public class UnavailableSurblAdapter implements SurblPort {

    @Override
    public Optional<BlacklistProviderObservation> query(String domainName) {
        return Optional.empty();
    }
}
