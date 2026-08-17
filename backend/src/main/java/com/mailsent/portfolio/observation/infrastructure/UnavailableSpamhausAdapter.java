package com.mailsent.portfolio.observation.infrastructure;

import java.util.Optional;

import com.mailsent.portfolio.observation.application.BlacklistProviderObservation;
import com.mailsent.portfolio.observation.application.SpamhausPort;

/**
 * Production Spamhaus adapter. No verified Spamhaus HTTP API, DNS zone,
 * credentials, or response schema exists in this repository, so this adapter
 * reports unavailability and never invents a blacklist fact.
 */
public class UnavailableSpamhausAdapter implements SpamhausPort {

    @Override
    public Optional<BlacklistProviderObservation> query(String domainName) {
        return Optional.empty();
    }
}
