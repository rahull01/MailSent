package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link DnsObservation} value object (INF-001) — deterministic
 * normalization and no-fabrication invariants.
 */
class DnsObservationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void normalizesMxByPriorityAndTxtLexicographically() {
        DnsObservation observation = DnsObservation.of("example.com",
            List.of(new MxRecord(20, "secondary.example.com"), new MxRecord(10, "primary.example.com")),
            List.of("v=spf1 -all", "google-site-verification=abc"),
            "v=spf1 -all", NOW);

        assertEquals(List.of("primary.example.com", "secondary.example.com"),
            observation.mxRecords().stream().map(MxRecord::exchange).toList(),
            "MX records ordered by priority");
        assertEquals(List.of("google-site-verification=abc", "v=spf1 -all"),
            observation.txtRecords(), "TXT strings sorted deterministically");
        assertEquals("v=spf1 -all", observation.spfRecord().orElseThrow());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void spfAbsentWhenNoSpfTxtRecord() {
        DnsObservation observation = DnsObservation.of("example.com",
            List.of(), List.of("dkim=test"), null, NOW);
        assertTrue(observation.spfRecord().isEmpty());
    }

    @Test
    void emptyRecordsAreValidObservation() {
        DnsObservation observation = DnsObservation.of("example.com", List.of(), List.of(), null, NOW);
        assertTrue(observation.mxRecords().isEmpty());
        assertTrue(observation.txtRecords().isEmpty());
    }

    @Test
    void requiresDomainName() {
        assertThrows(IllegalArgumentException.class,
            () -> DnsObservation.of("  ", List.of(), List.of(), null, NOW));
    }

    @Test
    void requiresObservedAt() {
        assertThrows(IllegalArgumentException.class,
            () -> DnsObservation.of("example.com", List.of(), List.of(), null, null));
    }
}