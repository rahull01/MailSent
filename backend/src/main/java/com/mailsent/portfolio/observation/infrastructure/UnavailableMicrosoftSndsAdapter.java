package com.mailsent.portfolio.observation.infrastructure;

import java.util.Optional;

import com.mailsent.portfolio.observation.application.MicrosoftSndsPort;
import com.mailsent.portfolio.observation.application.ReputationProviderObservation;

/**
 * Production Microsoft SNDS adapter. No verified SNDS HTTP API, credentials,
 * or response schema exists in this repository, so this adapter reports
 * unavailability and never invents a reputation fact.
 */
public class UnavailableMicrosoftSndsAdapter implements MicrosoftSndsPort {

    @Override
    public Optional<ReputationProviderObservation> query(String domainName) {
        return Optional.empty();
    }
}
