package com.mailsent.explanation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class GroundingTest {

    @Test
    void extractsEvidenceFromSignal() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
            
        Grounding grounding = Grounding.from(signal);
        
        assertEquals("hs-1", grounding.sourceSignalId());
        assertEquals("oe-1", grounding.observationEvidenceId());
        assertEquals(List.of("agg-1"), grounding.dmarcAggregateEvidenceIds());
        assertEquals(List.of("rep-1"), grounding.reputationObservationIds());
    }

    @Test
    void requiresSignal() {
        NullPointerException exception = assertThrows(
            NullPointerException.class, () -> Grounding.from(null)
        );
        assertEquals("Source signal is required for grounding.", exception.getMessage());
    }
}
