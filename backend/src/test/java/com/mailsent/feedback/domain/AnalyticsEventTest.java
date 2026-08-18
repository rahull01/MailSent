package com.mailsent.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AnalyticsEventTest {

    @Test
    void capturesAnalyticsEventSuccessfully() {
        Instant now = Instant.now();
        AnalyticsEvent event = AnalyticsEvent.capture(
                "org-1",
                AnalyticsEventType.SCAN_COMPLETED,
                "domain-123",
                "user-456",
                Map.of("durationMs", "1450", "status", "HEALTHY"),
                now
        );

        assertNotNull(event.id());
        assertEquals("org-1", event.organizationId());
        assertEquals(AnalyticsEventType.SCAN_COMPLETED, event.eventType());
        assertEquals("domain-123", event.entityId());
        assertEquals("user-456", event.userId());
        assertEquals("1450", event.metadata().get("durationMs"));
        assertEquals(now, event.occurredAt());
    }

    @Test
    void validatesBlankFields() {
        assertThrows(IllegalArgumentException.class, () ->
                AnalyticsEvent.capture("", AnalyticsEventType.SCAN_INITIATED, "ent-1", "usr-1", Map.of(), Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                AnalyticsEvent.capture("org-1", AnalyticsEventType.SCAN_INITIATED, "", "usr-1", Map.of(), Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                AnalyticsEvent.capture("org-1", AnalyticsEventType.SCAN_INITIATED, "ent-1", "", Map.of(), Instant.now())
        );
    }
}
