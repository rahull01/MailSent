package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import com.mailsent.portfolio.observation.domain.BlacklistSource;

/**
 * Test-only URIBL double. Never used in production.
 */
final class FakeUriblPort implements UriblPort {

    Optional<BlacklistProviderObservation> result = Optional.empty();
    Supplier<Optional<BlacklistProviderObservation>> failure;
    Instant observedAt = Instant.parse("2026-08-08T10:00:00Z");
    String payload = "uribl-raw";
    int queryCount;

    @Override
    public Optional<BlacklistProviderObservation> query(String domainName) {
        queryCount++;
        if (failure != null) {
            return failure.get();
        }
        return result;
    }

    void available() {
        failure = null;
        result = Optional.of(new BlacklistProviderObservation(BlacklistSource.URIBL, observedAt, payload));
    }

    void unavailable() {
        failure = null;
        result = Optional.empty();
    }

    void fail() {
        failure = () -> {
            throw new IllegalStateException("URIBL unavailable");
        };
    }
}
