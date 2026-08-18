package com.mailsent.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FeatureAdoptionScoreTest {

    @Test
    void calculatesAdoptionScoreAccurately() {
        Instant now = Instant.now();
        Map<String, Integer> usage = Map.of(
                "HEALTH_SCAN", 12,
                "EVIDENCE_EXPORT", 4,
                "SCHEDULED_REPORT", 1,
                "ALERT_CONFIG", 0
        );

        FeatureAdoptionScore score = FeatureAdoptionScore.calculate("org-1", usage, 4, now);

        assertNotNull(score.id());
        assertEquals("org-1", score.organizationId());
        assertEquals(75, score.score());
        assertEquals(3, score.activeFeaturesCount());
        assertEquals(4, score.totalFeaturesCount());
        assertEquals(now, score.calculatedAt());
    }

    @Test
    void calculatesZeroScoreWhenNoUsage() {
        FeatureAdoptionScore score = FeatureAdoptionScore.calculate("org-1", Map.of(), 5, Instant.now());
        assertEquals(0, score.score());
        assertEquals(0, score.activeFeaturesCount());
        assertEquals(5, score.totalFeaturesCount());
    }

    @Test
    void validatesInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                FeatureAdoptionScore.calculate("", Map.of(), 5, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                FeatureAdoptionScore.calculate("org-1", Map.of(), 0, Instant.now())
        );
    }
}
