package com.mailsent.alerts.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.alerts.domain.Alert;
import com.mailsent.alerts.domain.AlertSeverity;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;

/**
 * ALERT-002 transition-matrix and boundary tests (CTO decisions D1–D4).
 *
 * <p>Every approved severity cell is proven, first-observation policy is proven (including
 * explicit no-alert outcomes), business-language rules are enforced structurally, tenant
 * propagation is verbatim, and the generation-only boundary is proven reflectively.</p>
 */
class AlertGenerationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");
    private static final String ORG = "org-tenant-a";
    private static final String DOMAIN = "dom-1";

    private final AlertGenerationService service = new AlertGenerationService();

    private HealthChangeEvent event(HealthIndication previous, HealthIndication current) {
        return HealthChangeEvent.detect(ORG, DOMAIN, previous, current, NOW);
    }

    private Alert generateAlert(HealthIndication previous, HealthIndication current) {
        return service.generate(event(previous, current)).orElseThrow();
    }

    // ---------- D1 transition matrix ----------

    @Test
    void healthyToAtRiskIsCritical() {
        assertEquals(AlertSeverity.CRITICAL,
            generateAlert(HealthIndication.HEALTHY, HealthIndication.AT_RISK).severity());
    }

    @Test
    void unknownToAtRiskIsCritical() {
        assertEquals(AlertSeverity.CRITICAL,
            generateAlert(HealthIndication.UNKNOWN, HealthIndication.AT_RISK).severity());
    }

    @Test
    void healthyToUnknownIsWarning() {
        assertEquals(AlertSeverity.WARNING,
            generateAlert(HealthIndication.HEALTHY, HealthIndication.UNKNOWN).severity());
    }

    @Test
    void atRiskToUnknownIsWarning() {
        assertEquals(AlertSeverity.WARNING,
            generateAlert(HealthIndication.AT_RISK, HealthIndication.UNKNOWN).severity());
    }

    @Test
    void atRiskToHealthyIsInfo() {
        assertEquals(AlertSeverity.INFO,
            generateAlert(HealthIndication.AT_RISK, HealthIndication.HEALTHY).severity());
    }

    @Test
    void unknownToHealthyIsInfo() {
        assertEquals(AlertSeverity.INFO,
            generateAlert(HealthIndication.UNKNOWN, HealthIndication.HEALTHY).severity());
    }

    // ---------- first-observation policy ----------

    @Test
    void firstObservationOfAtRiskIsCritical() {
        assertEquals(AlertSeverity.CRITICAL,
            generateAlert(null, HealthIndication.AT_RISK).severity());
    }

    @Test
    void firstObservationOfHealthyGeneratesNoAlert() {
        assertTrue(service.generate(event(null, HealthIndication.HEALTHY)).isEmpty(),
            "a healthy first reading establishes a baseline — it is not a change (D1)");
    }

    @Test
    void firstObservationOfUnknownGeneratesNoAlert() {
        assertTrue(service.generate(event(null, HealthIndication.UNKNOWN)).isEmpty(),
            "an unknown first reading establishes a baseline — it is not a change (D1)");
    }

    // ---------- FUN-M4 business language ----------

    @Test
    void everyGeneratedAlertCarriesTheCompleteBusinessTriad() {
        for (var cell : new HealthIndication[][] {
                {HealthIndication.HEALTHY, HealthIndication.AT_RISK},
                {HealthIndication.UNKNOWN, HealthIndication.AT_RISK},
                {HealthIndication.HEALTHY, HealthIndication.UNKNOWN},
                {HealthIndication.AT_RISK, HealthIndication.UNKNOWN},
                {HealthIndication.AT_RISK, HealthIndication.HEALTHY},
                {HealthIndication.UNKNOWN, HealthIndication.HEALTHY},
                {null, HealthIndication.AT_RISK}}) {
            Alert alert = generateAlert(cell[0], cell[1]);
            assertNotNull(alert.whatHappened(), "what happened required: " + cell[0] + "->" + cell[1]);
            assertTrue(!alert.whatHappened().isBlank());
            assertNotNull(alert.whyItMatters(), "why it matters required: " + cell[0] + "->" + cell[1]);
            assertTrue(!alert.whyItMatters().isBlank());
            if (alert.severity() == AlertSeverity.INFO) {
                assertNull(alert.actionRequired(), "recovery notices are purely informational");
            } else {
                assertNotNull(alert.actionRequired(), "whether-to-act required: " + cell[0] + "->" + cell[1]);
            }
        }
    }

    @Test
    void businessLanguageLeadsWithoutFabricatedTechnicalCausesOrRemediation() {
        String[] forbiddenTokens = {"spf", "dkim", "dmarc", "mx ", "dns", "blacklist",
            "smtp", "configuration is wrong", "misconfigured"};
        for (var cell : new HealthIndication[][] {
                {HealthIndication.HEALTHY, HealthIndication.AT_RISK},
                {HealthIndication.UNKNOWN, HealthIndication.AT_RISK},
                {null, HealthIndication.AT_RISK},
                {HealthIndication.HEALTHY, HealthIndication.UNKNOWN},
                {HealthIndication.AT_RISK, HealthIndication.UNKNOWN},
                {HealthIndication.AT_RISK, HealthIndication.HEALTHY},
                {HealthIndication.UNKNOWN, HealthIndication.HEALTHY}}) {
            Alert alert = generateAlert(cell[0], cell[1]);
            assertBusinessSafe(alert.whatHappened(), forbiddenTokens);
            assertBusinessSafe(alert.whyItMatters(), forbiddenTokens);
            if (alert.actionRequired() != null) {
                assertBusinessSafe(alert.actionRequired(), forbiddenTokens);
            }
            // No fabricated remediation specifics (counts/providers/fixes):
            String haystack = (alert.whatHappened() + " " + alert.whyItMatters()).toLowerCase(Locale.ROOT);
            for (String invented : List.of("exceeded", "failed for", "caused by", "reset the", "change your")) {
                assertTrue(!haystack.contains(invented), "fabricated specificity '" + invented + "' in: " + haystack);
            }
        }
    }

    private static void assertBusinessSafe(String text, String[] forbiddenTokens) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String token : forbiddenTokens) {
            assertTrue(!lower.contains(token.trim()),
                "raw technical cause '" + token + "' must not appear in business language: '" + text + "'");
        }
    }

    // ---------- tenant scope & identity (D2) ----------

    @Test
    void organizationIdPropagatesVerbatimFromTheEvent() {
        Alert alert = generateAlert(HealthIndication.HEALTHY, HealthIndication.AT_RISK);
        assertEquals(ORG, alert.organizationId(),
            "the event's organization is the alert's authoritative tenant scope (INV-09)");
        assertTrue(alert.whatHappened().contains(DOMAIN) || alert.whatHappened().contains("'" + DOMAIN + "'"),
            "the domain reference in the body belongs to the same tenant's event");
    }

    @Test
    void incidentIdIsNullUnlessTheCallerSuppliesOne() {
        Alert withoutIncident = generateAlert(HealthIndication.HEALTHY, HealthIndication.AT_RISK);
        assertNull(withoutIncident.incidentId(), "health-change-originated alerts carry no incident (D2)");

        Alert withIncident = service.generate(
            event(HealthIndication.HEALTHY, HealthIndication.AT_RISK), "incident-77").orElseThrow();
        assertEquals("incident-77", withIncident.incidentId(),
            "incident context is linked exactly when supplied by the caller");
    }

    @Test
    void crossTenantIdentityIsNeverSubstituted() {
        HealthChangeEvent foreign = HealthChangeEvent.detect(
            "org-someone-else", DOMAIN, HealthIndication.HEALTHY, HealthIndication.AT_RISK, NOW);
        Alert alert = service.generate(foreign).orElseThrow();
        assertEquals("org-someone-else", alert.organizationId());
        assertTrue(!ORG.equals(alert.organizationId()),
            "the generator must never swap in a different tenant's identity");
    }

    // ---------- determinism & rejection ----------

    @Test
    void generationIsDeterministicPerEvent() {
        HealthChangeEvent e = event(HealthIndication.HEALTHY, HealthIndication.AT_RISK);
        Alert first = service.generate(e).orElseThrow();
        Alert second = service.generate(e).orElseThrow();
        assertEquals(first.severity(), second.severity());
        assertEquals(first.whatHappened(), second.whatHappened());
        assertEquals(first.whyItMatters(), second.whyItMatters());
        assertEquals(first.actionRequired(), second.actionRequired());
        assertEquals(first.createdAt(), second.createdAt());
        assertEquals(ORG, second.organizationId());
    }

    @Test
    void nullEventIsRejected() {
        assertThrows(NullPointerException.class, () -> service.generate(null));
        assertThrows(NullPointerException.class, () -> service.generate(null, "incident-1"));
    }

    // ---------- generation-only boundary (reflective proof) ----------

    @Test
    void generatorIsStatelessAndHasNoInfrastructureDependencies() {
        var fields = AlertGenerationService.class.getDeclaredFields();
        assertEquals(0, fields.length,
            "ALERT-002 is generation-only: no repositories, senders, or clients may be injected");
    }
}
