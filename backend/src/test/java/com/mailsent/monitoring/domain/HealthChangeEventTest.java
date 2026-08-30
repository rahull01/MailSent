package com.mailsent.monitoring.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;

class HealthChangeEventTest {

    @Test
    void detectsChangeWhenIndicationsDiffer() {
        HealthChangeEvent event = HealthChangeEvent.detect(
            "org-1", "dom-1", HealthIndication.HEALTHY, HealthIndication.AT_RISK, Instant.now()
        );

        assertNotNull(event);
        assertEquals("org-1", event.organizationId());
        assertEquals("dom-1", event.domainId());
        assertEquals(HealthIndication.HEALTHY, event.previousIndication());
        assertEquals(HealthIndication.AT_RISK, event.currentIndication());
    }

    @Test
    void detectsInitialSignal() {
        HealthChangeEvent event = HealthChangeEvent.detect(
            "org-1", "dom-1", null, HealthIndication.HEALTHY, Instant.now()
        );

        assertNotNull(event);
        assertEquals("org-1", event.organizationId());
        assertEquals("dom-1", event.domainId());
        assertNull(event.previousIndication());
        assertEquals(HealthIndication.HEALTHY, event.currentIndication());
    }

    @Test
    void returnsNullWhenNoChange() {
        HealthChangeEvent event = HealthChangeEvent.detect(
            "org-1", "dom-1", HealthIndication.HEALTHY, HealthIndication.HEALTHY, Instant.now()
        );

        assertNull(event);
    }
}
