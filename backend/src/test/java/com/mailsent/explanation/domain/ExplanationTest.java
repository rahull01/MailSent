package com.mailsent.explanation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * EXPL-001 grounding tests (N2/INV-04): explanations state only what the canonical
 * indication means. Fabricated causes, mechanisms, provider claims, and false certainty
 * are asserted absent.
 */
class ExplanationTest {

    private static final String[] FORBIDDEN_CLAIMS = {
        "misconfigured", "missing", "spf", "dkim", "dmarc", "dns",
        "blacklist", "enforcing", "no recent", "fully authenticated"
    };

    private HealthSignal signal(HealthIndication indication) {
        return HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            indication, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
    }

    private void assertGrounded(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String claim : FORBIDDEN_CLAIMS) {
            assertTrue(!lower.contains(claim),
                "fabricated/technical claim '" + claim + "' must not appear in: '" + text + "'");
        }
        assertTrue(!text.isBlank());
    }

    @Test
    void translatesAtRiskSignal() {
        HealthSignal signal = signal(HealthIndication.AT_RISK);

        Explanation explanation = Explanation.translate(signal);

        assertEquals("dom-1", explanation.domainId());
        assertEquals("hs-1", explanation.grounding().sourceSignalId());
        assertGrounded(explanation.whatHappened());
        assertGrounded(explanation.whyItMatters());
        assertGrounded(explanation.technicalDetail());
        // Approved AT_RISK semantics: problems are putting delivery at risk.
        assertTrue(explanation.whatHappened().toLowerCase(Locale.ROOT).contains("at risk"));
        assertTrue(explanation.whyItMatters().contains("may not reach"));
        // The unknown-state disclaimer must never leak into a determined state:
        assertTrue(!explanation.technicalDetail().toLowerCase(Locale.ROOT).contains("inconclusive"));
    }

    @Test
    void translatesHealthySignalWithoutOverclaimingEnforcement() {
        Explanation explanation = Explanation.translate(signal(HealthIndication.HEALTHY));

        assertGrounded(explanation.whatHappened());
        assertGrounded(explanation.whyItMatters());
        assertGrounded(explanation.technicalDetail());
        // HEALTH-001 healthy = all reported checks passed; policy enforcement mode is not evidence.
        String joined = (explanation.whatHappened() + " " + explanation.technicalDetail())
            .toLowerCase(Locale.ROOT);
        assertTrue(!joined.contains("enforc"), "healthy prose must not claim an enforcement policy");
    }

    @Test
    void translatesUnknownSignalAsExplicitInconclusive() {
        Explanation explanation = Explanation.translate(signal(HealthIndication.UNKNOWN));

        assertGrounded(explanation.whatHappened());
        assertGrounded(explanation.whyItMatters());
        assertGrounded(explanation.technicalDetail());
        // The old prose claimed "No recent DMARC aggregate reports have been received" —
        // UNKNOWN routinely occurs WITH reports; only inconclusiveness may be stated.
        assertTrue(explanation.technicalDetail().toLowerCase(Locale.ROOT).contains("inconclusive"));
        assertTrue(explanation.technicalDetail().contains("unknown"),
            "the unknown must be explicit, never dressed as a data-absence claim");
    }

    @Test
    void requiresSignal() {
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> Explanation.translate(null)
        );
        assertEquals("A health signal is required for explanation.", exception.getMessage());
    }
}
