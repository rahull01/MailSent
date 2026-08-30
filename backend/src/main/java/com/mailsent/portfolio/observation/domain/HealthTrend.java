package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * HealthTrend — derived value projection representing historical delivery-health trajectory (HEALTH-002).
 */
public final class HealthTrend {

    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final TrendDirection direction;
    private final HealthIndication currentIndication;
    private final HealthIndication previousIndication; // nullable if INSUFFICIENT_DATA
    private final List<String> evaluatedHealthSignalIds;
    private final Instant evaluatedAt;

    private HealthTrend(String organizationId, String domainId, String domainName,
                        TrendDirection direction, HealthIndication currentIndication,
                        HealthIndication previousIndication, List<String> evaluatedHealthSignalIds,
                        Instant evaluatedAt) {
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId cannot be null");
        this.domainId = Objects.requireNonNull(domainId, "domainId cannot be null");
        this.domainName = Objects.requireNonNull(domainName, "domainName cannot be null");
        this.direction = Objects.requireNonNull(direction, "direction cannot be null");
        this.currentIndication = currentIndication;
        this.previousIndication = previousIndication;
        this.evaluatedHealthSignalIds = List.copyOf(Objects.requireNonNull(evaluatedHealthSignalIds, "evaluatedHealthSignalIds cannot be null"));
        this.evaluatedAt = Objects.requireNonNull(evaluatedAt, "evaluatedAt cannot be null");
    }

    public static HealthTrend calculate(String organizationId, String domainId, String domainName,
                                        List<HealthSignal> historicalSignals, Instant evaluatedAt) {
        Instant now = evaluatedAt != null ? evaluatedAt : Instant.now();

        if (historicalSignals == null || historicalSignals.isEmpty()) {
            return new HealthTrend(organizationId, domainId, domainName, TrendDirection.INSUFFICIENT_DATA,
                    null, null, List.of(), now);
        }

        // Sort chronologically ascending (oldest first, latest last)
        List<HealthSignal> sorted = historicalSignals.stream()
                .sorted(Comparator.comparing(HealthSignal::observedAt).thenComparing(HealthSignal::id))
                .toList();

        List<String> signalIds = sorted.stream().map(HealthSignal::id).toList();
        HealthSignal latest = sorted.get(sorted.size() - 1);
        HealthIndication currentIndication = latest.indication();

        if (sorted.size() < 2) {
            return new HealthTrend(organizationId, domainId, domainName, TrendDirection.INSUFFICIENT_DATA,
                    currentIndication, null, signalIds, now);
        }

        if (currentIndication == HealthIndication.UNKNOWN) {
            return new HealthTrend(organizationId, domainId, domainName, TrendDirection.INSUFFICIENT_DATA,
                    currentIndication, null, signalIds, now);
        }

        // Look backward for the most recent valid baseline signal (prior distinct non-unknown or prior signal)
        HealthSignal previousSignal = null;
        for (int i = sorted.size() - 2; i >= 0; i--) {
            HealthSignal candidate = sorted.get(i);
            if (candidate.indication() != HealthIndication.UNKNOWN) {
                previousSignal = candidate;
                break;
            }
        }

        if (previousSignal == null) {
            // All prior signals are UNKNOWN -> insufficient baseline
            return new HealthTrend(organizationId, domainId, domainName, TrendDirection.INSUFFICIENT_DATA,
                    currentIndication, null, signalIds, now);
        }

        HealthIndication prevIndication = previousSignal.indication();
        TrendDirection direction;

        if (currentIndication == HealthIndication.HEALTHY && prevIndication == HealthIndication.AT_RISK) {
            direction = TrendDirection.IMPROVING;
        } else if (currentIndication == HealthIndication.AT_RISK && prevIndication == HealthIndication.HEALTHY) {
            direction = TrendDirection.DECLINING;
        } else if (currentIndication == prevIndication) {
            direction = TrendDirection.STABLE;
        } else {
            direction = TrendDirection.INSUFFICIENT_DATA;
        }

        return new HealthTrend(organizationId, domainId, domainName, direction,
                currentIndication, prevIndication, signalIds, now);
    }

    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public String domainName() { return domainName; }
    public TrendDirection direction() { return direction; }
    public HealthIndication currentIndication() { return currentIndication; }
    public HealthIndication previousIndication() { return previousIndication; }
    public List<String> evaluatedHealthSignalIds() { return evaluatedHealthSignalIds; }
    public Instant evaluatedAt() { return evaluatedAt; }
}
