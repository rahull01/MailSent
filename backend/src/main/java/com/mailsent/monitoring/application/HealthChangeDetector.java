package com.mailsent.monitoring.application;

import java.time.Instant;
import java.util.Optional;

import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthSignal;

public class HealthChangeDetector {

    private final HealthChangeEventRepository events;
    private final IncidentService incidents;

    public HealthChangeDetector(HealthChangeEventRepository events, IncidentService incidents) {
        this.events = events;
        this.incidents = incidents;
    }

    public Optional<HealthChangeEvent> detectChange(Optional<HealthSignal> previous, HealthSignal current, Instant detectedAt) {
        if (current == null) {
            throw new IllegalArgumentException("Current signal is required");
        }

        HealthChangeEvent event;
        if (previous.isEmpty()) {
            event = HealthChangeEvent.detect(current.domainId(), null, current.indication(), detectedAt);
        } else {
            event = HealthChangeEvent.detect(current.domainId(), previous.get().indication(), current.indication(), detectedAt);
        }

        if (event != null) {
            events.save(event);
            incidents.processHealthChange(event);
            return Optional.of(event);
        }
        return Optional.empty();
    }
}
