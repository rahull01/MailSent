package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

class HealthSignalTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00.123456789Z");

    @Test
    void deriveTakesIdentityFromRegisteredDomainAndTruncatesObservedAtToMicros() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        HealthSignal signal = HealthSignal.derive(
            domain, HealthIndication.UNKNOWN, "oe-1", List.of("agg-1"), List.of("rep-1"), NOW);

        assertEquals("org-1", signal.organizationId());
        assertEquals(domain.id(), signal.domainId());
        assertEquals("example.com", signal.domainName());
        assertEquals(HealthIndication.UNKNOWN, signal.indication());
        assertEquals("oe-1", signal.observationEvidenceId());
        assertEquals(List.of("agg-1"), signal.dmarcAggregateEvidenceIds());
        assertEquals(List.of("rep-1"), signal.reputationObservationIds());
        assertEquals(NOW.truncatedTo(ChronoUnit.MICROS), signal.observedAt());
    }

    @Test
    void deriveRequiresDomainIndicationLineageAndObservedAt() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class, () -> HealthSignal.derive(
            null, HealthIndication.UNKNOWN, "oe-1", List.of("agg-1"), List.of("rep-1"), NOW));
        assertThrows(IllegalArgumentException.class, () -> HealthSignal.derive(
            domain, null, "oe-1", List.of("agg-1"), List.of("rep-1"), NOW));
        assertThrows(IllegalArgumentException.class, () -> HealthSignal.derive(
            domain, HealthIndication.UNKNOWN, " ", List.of("agg-1"), List.of("rep-1"), NOW));
        assertThrows(IllegalArgumentException.class, () -> HealthSignal.derive(
            domain, HealthIndication.UNKNOWN, "oe-1", List.of(), List.of("rep-1"), NOW));
        assertThrows(IllegalArgumentException.class, () -> HealthSignal.derive(
            domain, HealthIndication.UNKNOWN, "oe-1", List.of("agg-1"), List.of(), NOW));
        assertThrows(IllegalArgumentException.class, () -> HealthSignal.derive(
            domain, HealthIndication.UNKNOWN, "oe-1", List.of("agg-1"), List.of("rep-1"), null));
    }

    @Test
    void restoreAdoptsStoredFacts() {
        HealthSignal signal = HealthSignal.restore(
            "hs-1", "org-1", "dom-1", "example.com", HealthIndication.UNKNOWN, "oe-1",
            List.of("agg-1", "agg-2"), List.of("rep-1"), NOW.truncatedTo(ChronoUnit.MICROS));
        assertEquals("hs-1", signal.id());
        assertEquals(List.of("agg-1", "agg-2"), signal.dmarcAggregateEvidenceIds());
    }
}
