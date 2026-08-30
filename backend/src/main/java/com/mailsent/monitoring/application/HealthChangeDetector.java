package com.mailsent.monitoring.application;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.mailsent.alerts.application.AlertGenerationService;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * HealthChangeDetector — MON-002: turns a realized health-signal change into a persisted
 * {@link HealthChangeEvent}, runs the existing incident processing, and hands the event to
 * the {@link AlertGenerationService} (ALERT-002, CTO decision D4: synchronous generation
 * on this seam; no queues, no new infrastructure).
 *
 * <p>The detector owns change detection only. Generated alerts are the pure result of the
 * generator — this class neither stores nor sends them (D3: ALERT-002 is generation-only).</p>
 */
public class HealthChangeDetector {

    private final HealthChangeEventRepository events;
    private final IncidentService incidents;
    private final AlertGenerationService alerts;

    public HealthChangeDetector(HealthChangeEventRepository events, IncidentService incidents,
                                AlertGenerationService alerts) {
        this.events = Objects.requireNonNull(events, "events repository is required");
        this.incidents = Objects.requireNonNull(incidents, "incident service is required");
        this.alerts = Objects.requireNonNull(alerts, "alert generation service is required");
    }

    public Optional<HealthChangeEvent> detectChange(Optional<HealthSignal> previous, HealthSignal current, Instant detectedAt) {
        if (current == null) {
            throw new IllegalArgumentException("Current signal is required");
        }

        HealthChangeEvent event;
        if (previous.isEmpty()) {
            event = HealthChangeEvent.detect(current.organizationId(), current.domainId(), null, current.indication(), detectedAt);
        } else {
            event = HealthChangeEvent.detect(current.organizationId(), current.domainId(), previous.get().indication(), current.indication(), detectedAt);
        }

        if (event != null) {
            events.save(event);
            incidents.processHealthChange(event);
            // ALERT-002 (generation-only): the resulting alert is intentionally not
            // persisted or delivered here — those are later milestones' responsibilities.
            alerts.generate(event);
            return Optional.of(event);
        }
        return Optional.empty();
    }
}
