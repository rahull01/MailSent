package com.mailsent.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class TimeToValueMetricTest {

    @Test
    void recordsMetricSuccessfully() {
        Instant now = Instant.now();
        TimeToValueMetric metric = TimeToValueMetric.record(
                "org-1",
                "domain-1",
                WorkflowStage.VERIFIED_HEALTHY,
                3600L,
                true,
                now
        );

        assertNotNull(metric.id());
        assertEquals("org-1", metric.organizationId());
        assertEquals("domain-1", metric.domainId());
        assertEquals(WorkflowStage.VERIFIED_HEALTHY, metric.stage());
        assertEquals(3600L, metric.durationSeconds());
        assertTrue(metric.completed());
        assertEquals(now, metric.recordedAt());
    }

    @Test
    void validatesArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                TimeToValueMetric.record("", "domain-1", WorkflowStage.ONBOARDED, 0L, true, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                TimeToValueMetric.record("org-1", "", WorkflowStage.ONBOARDED, 0L, true, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                TimeToValueMetric.record("org-1", "domain-1", WorkflowStage.ONBOARDED, -5L, true, Instant.now())
        );
    }
}
