package com.mailsent.portfolio.observation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class HealthTrendTest {

    private static final Instant T0 = Instant.parse("2026-08-10T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-08-11T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-08-12T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-08-15T12:00:00Z");

    private HealthSignal signal(String id, HealthIndication indication, Instant observedAt) {
        return HealthSignal.restore(id, "org-1", "dom-1", "example.com",
                indication, "oe-1", List.of("ev-1"), List.of("rep-1"), observedAt);
    }

    @Test
    void shouldCalculateImprovingTrendWhenTransitioningFromAtRiskToHealthy() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.AT_RISK, T0),
                signal("hs-2", HealthIndication.HEALTHY, T1)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.IMPROVING);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.previousIndication()).isEqualTo(HealthIndication.AT_RISK);
        assertThat(trend.evaluatedHealthSignalIds()).containsExactly("hs-1", "hs-2");
        assertThat(trend.evaluatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldCalculateDecliningTrendWhenTransitioningFromHealthyToAtRisk() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.HEALTHY, T0),
                signal("hs-2", HealthIndication.AT_RISK, T1)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.DECLINING);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.AT_RISK);
        assertThat(trend.previousIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.evaluatedHealthSignalIds()).containsExactly("hs-1", "hs-2");
    }

    @Test
    void shouldCalculateStableTrendWhenConsistentlyHealthy() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.HEALTHY, T0),
                signal("hs-2", HealthIndication.HEALTHY, T1),
                signal("hs-3", HealthIndication.HEALTHY, T2)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.STABLE);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.previousIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.evaluatedHealthSignalIds()).containsExactly("hs-1", "hs-2", "hs-3");
    }

    @Test
    void shouldCalculateStableTrendWhenConsistentlyAtRisk() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.AT_RISK, T0),
                signal("hs-2", HealthIndication.AT_RISK, T1)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.STABLE);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.AT_RISK);
        assertThat(trend.previousIndication()).isEqualTo(HealthIndication.AT_RISK);
        assertThat(trend.evaluatedHealthSignalIds()).containsExactly("hs-1", "hs-2");
    }

    @Test
    void shouldReturnInsufficientDataWhenFewerThanTwoSnapshots() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.HEALTHY, T0)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.INSUFFICIENT_DATA);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.previousIndication()).isNull();
        assertThat(trend.evaluatedHealthSignalIds()).containsExactly("hs-1");
    }

    @Test
    void shouldReturnInsufficientDataWhenLatestIndicationIsUnknown() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.HEALTHY, T0),
                signal("hs-2", HealthIndication.UNKNOWN, T1)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.INSUFFICIENT_DATA);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.UNKNOWN);
        assertThat(trend.previousIndication()).isNull();
    }

    @Test
    void shouldReturnInsufficientDataWhenAllHistoricalAreUnknown() {
        List<HealthSignal> signals = List.of(
                signal("hs-1", HealthIndication.UNKNOWN, T0),
                signal("hs-2", HealthIndication.HEALTHY, T1)
        );

        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", signals, NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.INSUFFICIENT_DATA);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.previousIndication()).isNull();
    }

    @Test
    void shouldReturnInsufficientDataWhenEmptySignalsList() {
        HealthTrend trend = HealthTrend.calculate("org-1", "dom-1", "example.com", List.of(), NOW);

        assertThat(trend.direction()).isEqualTo(TrendDirection.INSUFFICIENT_DATA);
        assertThat(trend.currentIndication()).isNull();
        assertThat(trend.previousIndication()).isNull();
        assertThat(trend.evaluatedHealthSignalIds()).isEmpty();
    }
}
