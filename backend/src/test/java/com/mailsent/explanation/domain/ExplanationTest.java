package com.mailsent.explanation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class ExplanationTest {

    @Test
    void translatesAtRiskSignal() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
            
        Explanation explanation = Explanation.translate(signal);
        
        assertEquals("dom-1", explanation.domainId());
        assertEquals("hs-1", explanation.grounding().sourceSignalId());
        assertTrue(explanation.whatHappened().contains("at risk"));
        assertTrue(explanation.whyItMatters().contains("quarantined or rejected"));
    }

    @Test
    void translatesHealthySignal() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
            
        Explanation explanation = Explanation.translate(signal);
        
        assertTrue(explanation.whatHappened().contains("fully authenticated"));
        assertTrue(explanation.whyItMatters().contains("trust emails"));
    }

    @Test
    void translatesUnknownSignal() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.UNKNOWN, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
            
        Explanation explanation = Explanation.translate(signal);
        
        assertTrue(explanation.whatHappened().contains("sufficient recent data"));
        assertTrue(explanation.whyItMatters().contains("Without sufficient data"));
    }

    @Test
    void requiresSignal() {
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> Explanation.translate(null)
        );
        assertEquals("A health signal is required for explanation.", exception.getMessage());
    }
}
