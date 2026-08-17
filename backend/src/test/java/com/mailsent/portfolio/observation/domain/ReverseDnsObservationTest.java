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
 * Unit tests for {@link ReverseDnsObservation} (INF-005) — capture from Domain +
 * observed MX/IP/PTR facts, restore, and the absence of FCrDNS/health/score fields.
 */
class ReverseDnsObservationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void captureTakesIdentityFromDomainAndPreservesMxIpPtrAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        ReverseDnsObservation observation = ReverseDnsObservation.capture(
            domain, "mail.example.com", "192.0.2.10", IpAddressFamily.IPV4,
            List.of("ptr.example.net"), NOW);

        assertEquals("org-1", observation.organizationId());
        assertEquals(domain.id(), observation.domainId());
        assertEquals("example.com", observation.domainName());
        assertEquals("mail.example.com", observation.sourceMxHostname());
        assertEquals("192.0.2.10", observation.ip());
        assertEquals(IpAddressFamily.IPV4, observation.addressFamily());
        assertEquals(List.of("ptr.example.net"), observation.ptrValues());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void restoreAdoptsStoredValuesVerbatimIncludingEmptyPtrList() {
        ReverseDnsObservation observation = ReverseDnsObservation.restore(
            "rdns-1", "org-1", "dom-1", "example.com", "mx.example.com", "2001:db8::1",
            IpAddressFamily.IPV6, List.of(), NOW);

        assertEquals("rdns-1", observation.id());
        assertEquals("org-1", observation.organizationId());
        assertEquals("dom-1", observation.domainId());
        assertEquals(IpAddressFamily.IPV6, observation.addressFamily());
        assertTrue(observation.ptrValues().isEmpty());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void captureRequiresDomainMxIpFamilyPtrListAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class,
            () -> ReverseDnsObservation.capture(null, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, List.of(), NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReverseDnsObservation.capture(domain, " ", "192.0.2.1",
                IpAddressFamily.IPV4, List.of(), NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReverseDnsObservation.capture(domain, "mail.example.com", "",
                IpAddressFamily.IPV4, List.of(), NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReverseDnsObservation.capture(domain, "mail.example.com", "192.0.2.1",
                null, List.of(), NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReverseDnsObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, null, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> ReverseDnsObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, List.of(), null));
    }

    @Test
    void surfaceHasNoFcrdnsHealthPassFailOrScoreFields() {
        Set<String> names = Arrays.stream(ReverseDnsObservation.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("fcrdns")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("health")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("pass")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("fail")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("score")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("valid")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("diagnos")));
    }
}
