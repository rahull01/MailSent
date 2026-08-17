package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * Unit tests for {@link DnsHealthObservation} (INF-007) — capture from Domain +
 * facts, restore, and the absence of inferred health/validity fields.
 */
class DnsHealthObservationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void captureTakesIdentityFromDomainAndPreservesTtlDnssecAndPropagation() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DnsHealthFacts facts = sampleFacts(NOW);

        DnsHealthObservation observation = DnsHealthObservation.capture(domain, facts, NOW);

        assertEquals("org-1", observation.organizationId());
        assertEquals(domain.id(), observation.domainId());
        assertEquals("example.com", observation.domainName());
        assertEquals(NOW, observation.observedAt());
        assertEquals(facts.ttlObservations(), observation.ttlObservations());
        assertEquals(facts.dnssecObservations(), observation.dnssecObservations());
        assertEquals(facts.propagationObservations(), observation.propagationObservations());
        assertEquals("MX", observation.ttlObservations().get(0).recordType());
        assertEquals("example.com", observation.ttlObservations().get(0).owner());
    }

    @Test
    void restoreAdoptsStoredValuesVerbatim() {
        DnsHealthObservation observation = DnsHealthObservation.restore(
            "dns-1", "org-1", "dom-1", "example.com", NOW,
            List.of(), List.of(), List.of());

        assertEquals("dns-1", observation.id());
        assertEquals("org-1", observation.organizationId());
        assertEquals("dom-1", observation.domainId());
        assertTrue(observation.ttlObservations().isEmpty());
        assertTrue(observation.dnssecObservations().isEmpty());
        assertTrue(observation.propagationObservations().isEmpty());
    }

    @Test
    void captureRequiresDomainFactsAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DnsHealthFacts facts = sampleFacts(NOW);
        assertThrows(IllegalArgumentException.class, () -> DnsHealthObservation.capture(null, facts, NOW));
        assertThrows(IllegalArgumentException.class, () -> DnsHealthObservation.capture(domain, null, NOW));
        assertThrows(IllegalArgumentException.class, () -> DnsHealthObservation.capture(domain, facts, null));
    }

    @Test
    void surfaceHasNoInferredHealthOrValidityFields() {
        Set<String> names = Arrays.stream(DnsHealthObservation.class.getDeclaredMethods())
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
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("recommend")));
    }

    @Test
    void unavailableResolverMustNotCarryAnswers() {
        assertThrows(IllegalArgumentException.class, () -> new PropagationObservation(
            "8.8.8.8", "MX", "example.com", false, List.of(new PropagationAnswer("10 mail.example.com.", 60))));
    }

    @Test
    void hasRealObservationIsFalseWhenEverythingIsUnavailable() {
        DnsHealthFacts empty = DnsHealthFacts.of(NOW, List.of(),
            List.of(new DnssecObservation("DNSKEY", DnssecPresence.UNAVAILABLE, List.of())),
            List.of(PropagationObservation.unavailable("system", "MX", "example.com")));
        assertTrue(!empty.hasRealObservation());
    }

    static DnsHealthFacts sampleFacts(Instant observedAt) {
        return DnsHealthFacts.of(observedAt,
            List.of(new TtlObservation("MX", "example.com", "10 mail.example.com.", 3600)),
            List.of(new DnssecObservation("DNSKEY", DnssecPresence.PRESENT,
                List.of(new DnssecRecordFact("example.com", "257 3 8 ABC")))),
            List.of(new PropagationObservation("system", "MX", "example.com", true,
                List.of(new PropagationAnswer("10 mail.example.com.", 3600)))));
    }
}
