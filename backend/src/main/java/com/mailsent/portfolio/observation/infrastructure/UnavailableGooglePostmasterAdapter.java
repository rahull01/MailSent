package com.mailsent.portfolio.observation.infrastructure;

import java.util.Optional;

import com.mailsent.portfolio.observation.application.GooglePostmasterPort;
import com.mailsent.portfolio.observation.application.ReputationProviderObservation;

/**
 * Production Google Postmaster adapter. No verified Postmaster HTTP API,
 * credentials, or response schema exists in this repository, so this adapter
 * reports unavailability and never invents a reputation fact.
 */
public class UnavailableGooglePostmasterAdapter implements GooglePostmasterPort {

    @Override
    public Optional<ReputationProviderObservation> query(String domainName) {
        return Optional.empty();
    }
}
