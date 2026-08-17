package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Test-only Microsoft SNDS double. Never used in production.
 */
final class FakeMicrosoftSndsPort implements MicrosoftSndsPort {

    Optional<ReputationProviderObservation> result = Optional.empty();
    Supplier<Optional<ReputationProviderObservation>> failure;
    Instant observedAt = Instant.parse("2026-08-08T10:00:00Z");
    String payload = "snds-raw";

    @Override
    public Optional<ReputationProviderObservation> query(String domainName) {
        if (failure != null) {
            return failure.get();
        }
        return result;
    }

    void available() {
        failure = null;
        result = Optional.of(new ReputationProviderObservation(payload, observedAt));
    }

    void unavailable() {
        failure = null;
        result = Optional.empty();
    }

    void fail() {
        failure = () -> {
            throw new IllegalStateException("SNDS unavailable");
        };
    }
}
