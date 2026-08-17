package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * Unit tests for {@link BlacklistObservation} (INF-008) — capture from Domain +
 * provider fact, restore, and the absence of inferred health/reputation fields.
 */
class BlacklistObservationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void captureTakesIdentityFromDomainAndPreservesSourcePayloadAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        BlacklistObservation observation = BlacklistObservation.capture(
            domain, BlacklistSource.SPAMHAUS, "raw-spamhaus", NOW);

        assertEquals("org-1", observation.organizationId());
        assertEquals(domain.id(), observation.domainId());
        assertEquals("example.com", observation.domainName());
        assertEquals(BlacklistSource.SPAMHAUS, observation.source());
        assertEquals("raw-spamhaus", observation.payload());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void restoreAdoptsStoredValuesVerbatim() {
        BlacklistObservation observation = BlacklistObservation.restore(
            "bl-1", "org-1", "dom-1", "example.com", BlacklistSource.URIBL, NOW, "raw-uribl");

        assertEquals("bl-1", observation.id());
        assertEquals("org-1", observation.organizationId());
        assertEquals("dom-1", observation.domainId());
        assertEquals(BlacklistSource.URIBL, observation.source());
        assertEquals("raw-uribl", observation.payload());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void captureRequiresDomainSourcePayloadAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class,
            () -> BlacklistObservation.capture(null, BlacklistSource.SPAMHAUS, "raw", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> BlacklistObservation.capture(domain, null, "raw", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> BlacklistObservation.capture(domain, BlacklistSource.SPAMHAUS, null, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> BlacklistObservation.capture(domain, BlacklistSource.SPAMHAUS, "raw", null));
    }

    @Test
    void surfaceHasNoInferredHealthOrValidityFields() {
        Set<String> names = Arrays.stream(BlacklistObservation.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("health")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("valid")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("diagnos")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("finding")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("assess")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("score")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("grade")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("risk")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("complian")));
    }
}
