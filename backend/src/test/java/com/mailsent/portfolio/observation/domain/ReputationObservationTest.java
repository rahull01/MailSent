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
 * Unit tests for {@link ReputationObservation} (INF-003) — capture from Domain +
 * provider fact, restore, and the absence of inferred health/reputation fields.
 */
class ReputationObservationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void captureTakesIdentityFromDomainAndPreservesSourcePayloadAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        ReputationObservation observation = ReputationObservation.capture(
            domain, ReputationSource.POSTMASTER, "raw-postmaster", NOW);

        assertEquals("org-1", observation.organizationId());
        assertEquals(domain.id(), observation.domainId());
        assertEquals("example.com", observation.domainName());
        assertEquals(ReputationSource.POSTMASTER, observation.source());
        assertEquals("raw-postmaster", observation.payload());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void restoreAdoptsStoredValuesVerbatim() {
        ReputationObservation observation = ReputationObservation.restore(
            "rep-1", "org-1", "dom-1", "example.com", ReputationSource.SNDS, NOW, "raw-snds");

        assertEquals("rep-1", observation.id());
        assertEquals("org-1", observation.organizationId());
        assertEquals("dom-1", observation.domainId());
        assertEquals(ReputationSource.SNDS, observation.source());
        assertEquals("raw-snds", observation.payload());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void captureRequiresDomainSourcePayloadAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class,
            () -> ReputationObservation.capture(null, ReputationSource.POSTMASTER, "raw", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReputationObservation.capture(domain, null, "raw", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReputationObservation.capture(domain, ReputationSource.POSTMASTER, null, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReputationObservation.capture(domain, ReputationSource.POSTMASTER, "raw", null));
    }

    @Test
    void surfaceHasNoInferredHealthOrValidityFields() {
        Set<String> names = Arrays.stream(ReputationObservation.class.getDeclaredMethods())
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
