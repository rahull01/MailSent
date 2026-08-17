package com.mailsent.monitoring.application;

import java.time.Instant;
import java.util.Optional;

import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthSignal;

public class HealthChangeDetector {

    public Optional<HealthChangeEvent> detectChange(Optional<HealthSignal> previous, HealthSignal current, Instant detectedAt) {
        if (current == null) {
            throw new IllegalArgumentException("Current signal is required");
        }

        if (previous.isEmpty()) {
            return Optional.ofNullable(HealthChangeEvent.detect(current.domainId(), null, current.indication(), detectedAt));
        }

        return Optional.ofNullable(HealthChangeEvent.detect(
            current.domainId(),
            previous.get().indication(),
            current.indication(),
            detectedAt
        ));
    }
}
