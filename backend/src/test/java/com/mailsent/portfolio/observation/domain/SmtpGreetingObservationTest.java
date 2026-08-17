package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Unit tests for {@link SmtpGreetingObservation} (INF-006) — capture from Domain +
 * observed MX/IP/outcome/greeting facts, restore, and the absence of
 * health/validation/relay fields.
 */
class SmtpGreetingObservationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void captureTakesIdentityFromDomainAndPreservesMxIpOutcomeGreetingAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        SmtpGreetingObservation observation = SmtpGreetingObservation.capture(
            domain, "mail.example.com", "192.0.2.10", IpAddressFamily.IPV4,
            SmtpProbeOutcome.CONNECTED, "220 mail.example.com ESMTP", "250-PIPELINING\n250 HELP", NOW);

        assertEquals("org-1", observation.organizationId());
        assertEquals(domain.id(), observation.domainId());
        assertEquals("example.com", observation.domainName());
        assertEquals("mail.example.com", observation.sourceMxHostname());
        assertEquals("192.0.2.10", observation.ip());
        assertEquals(IpAddressFamily.IPV4, observation.addressFamily());
        assertEquals(SmtpProbeOutcome.CONNECTED, observation.probeOutcome());
        assertEquals("220 mail.example.com ESMTP", observation.rawGreeting());
        assertEquals("250-PIPELINING\n250 HELP", observation.rawEhlo());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void restoreAdoptsStoredValuesVerbatimIncludingEmptyGreeting() {
        SmtpGreetingObservation observation = SmtpGreetingObservation.restore(
            "smtp-1", "org-1", "dom-1", "example.com", "mx.example.com", "2001:db8::1",
            IpAddressFamily.IPV6, SmtpProbeOutcome.CONNECTED, "", "", NOW);

        assertEquals("smtp-1", observation.id());
        assertEquals("org-1", observation.organizationId());
        assertEquals("dom-1", observation.domainId());
        assertEquals(IpAddressFamily.IPV6, observation.addressFamily());
        assertEquals("", observation.rawGreeting());
        assertEquals("", observation.rawEhlo());
        assertEquals(NOW, observation.observedAt());
    }

    @Test
    void captureAllowsSuccessfulEmptyGreetingAndEhlo() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        SmtpGreetingObservation observation = SmtpGreetingObservation.capture(
            domain, "mail.example.com", "192.0.2.1", IpAddressFamily.IPV4,
            SmtpProbeOutcome.CONNECTED, "", null, NOW);
        assertEquals(SmtpProbeOutcome.CONNECTED, observation.probeOutcome());
        assertEquals("", observation.rawGreeting());
        assertNull(observation.rawEhlo());
    }

    @Test
    void captureRejectsFabricatedGreetingOnUnavailableOutcomes() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, SmtpProbeOutcome.REFUSED, "220 fake", null, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, SmtpProbeOutcome.TIMEOUT, null, "250 fake", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, SmtpProbeOutcome.UNAVAILABLE, "220 fake", "250 fake", NOW));
    }

    @Test
    void captureRequiresDomainMxIpFamilyOutcomeAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(null, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, SmtpProbeOutcome.CONNECTED, "", "", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, " ", "192.0.2.1",
                IpAddressFamily.IPV4, SmtpProbeOutcome.CONNECTED, "", "", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "",
                IpAddressFamily.IPV4, SmtpProbeOutcome.CONNECTED, "", "", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "192.0.2.1",
                null, SmtpProbeOutcome.CONNECTED, "", "", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, null, "", "", NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SmtpGreetingObservation.capture(domain, "mail.example.com", "192.0.2.1",
                IpAddressFamily.IPV4, SmtpProbeOutcome.CONNECTED, "", "", null));
    }

    @Test
    void surfaceHasNoHealthValidationRelayOrScoreFields() {
        Set<String> names = Arrays.stream(SmtpGreetingObservation.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("health")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("valid")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("score")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("relay")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("diagnos")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("auth")));
    }
}
