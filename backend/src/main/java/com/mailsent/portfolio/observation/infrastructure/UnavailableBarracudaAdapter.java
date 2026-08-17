package com.mailsent.portfolio.observation.infrastructure;

import java.util.Optional;

import com.mailsent.portfolio.observation.application.BarracudaPort;
import com.mailsent.portfolio.observation.application.BlacklistProviderObservation;

/**
 * Production Barracuda adapter. No verified Barracuda HTTP API, DNS zone,
 * credentials, or response schema exists in this repository, so this adapter
 * reports unavailability and never invents a blacklist fact.
 */
public class UnavailableBarracudaAdapter implements BarracudaPort {

    @Override
    public Optional<BlacklistProviderObservation> query(String domainName) {
        return Optional.empty();
    }
}
