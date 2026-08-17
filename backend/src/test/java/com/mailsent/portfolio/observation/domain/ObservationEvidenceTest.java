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
 * Unit tests for {@link ObservationEvidence} (INF-002) — capture from Domain +
 * DnsObservation, restore, and the absence of inferred health/validity fields.
 */
class ObservationEvidenceTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void captureTakesIdentityFromDomainAndRecordsFromDnsObservation() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DnsObservation dns = DnsObservation.of("example.com",
            List.of(new MxRecord(10, "mail.example.com")),
            List.of("v=spf1 -all"),
            "v=spf1 -all", NOW);

        ObservationEvidence evidence = ObservationEvidence.capture(domain, dns);

        assertEquals("org-1", evidence.organizationId());
        assertEquals(domain.id(), evidence.domainId());
        assertEquals("example.com", evidence.domainName());
        assertEquals(NOW, evidence.observedAt());
        assertEquals(List.of(new MxRecord(10, "mail.example.com")), evidence.mxRecords());
        assertEquals(List.of("v=spf1 -all"), evidence.txtRecords());
        assertEquals("v=spf1 -all", evidence.spfRecord().orElseThrow());
    }

    @Test
    void restoreAdoptsStoredValuesVerbatim() {
        ObservationEvidence evidence = ObservationEvidence.restore(
            "ev-1", "org-1", "dom-1", "example.com", NOW,
            List.of(new MxRecord(20, "backup.example.com")),
            List.of("dkim=test"),
            null);

        assertEquals("ev-1", evidence.id());
        assertEquals("org-1", evidence.organizationId());
        assertEquals("dom-1", evidence.domainId());
        assertTrue(evidence.spfRecord().isEmpty());
        assertEquals(List.of("dkim=test"), evidence.txtRecords());
    }

    @Test
    void captureRequiresDomainAndObservation() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DnsObservation dns = DnsObservation.of("example.com", List.of(), List.of(), null, NOW);
        assertThrows(IllegalArgumentException.class, () -> ObservationEvidence.capture(null, dns));
        assertThrows(IllegalArgumentException.class, () -> ObservationEvidence.capture(domain, null));
    }

    @Test
    void surfaceHasNoInferredHealthOrValidityFields() {
        Set<String> names = Arrays.stream(ObservationEvidence.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("health")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("valid")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("diagnos")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("finding")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("assess")));
    }
}
