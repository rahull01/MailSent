package com.mailsent.monitoring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class HealthChangeDetectorTest {

    private final HealthChangeDetector detector = new HealthChangeDetector();

    @Test
    void detectsChangeWhenIndicationsDiffer() {
        HealthSignal previous = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of(), List.of(), Instant.now());
        HealthSignal current = HealthSignal.restore("hs-2", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-2", List.of(), List.of(), Instant.now());

        Optional<HealthChangeEvent> eventOpt = detector.detectChange(Optional.of(previous), current, Instant.now());

        assertTrue(eventOpt.isPresent());
        HealthChangeEvent event = eventOpt.get();
        assertEquals("dom-1", event.domainId());
        assertEquals(HealthIndication.HEALTHY, event.previousIndication());
        assertEquals(HealthIndication.AT_RISK, event.currentIndication());
    }

    @Test
    void detectsInitialSignal() {
        HealthSignal current = HealthSignal.restore("hs-2", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-2", List.of(), List.of(), Instant.now());

        Optional<HealthChangeEvent> eventOpt = detector.detectChange(Optional.empty(), current, Instant.now());

        assertTrue(eventOpt.isPresent());
        HealthChangeEvent event = eventOpt.get();
        assertEquals("dom-1", event.domainId());
        assertEquals(HealthIndication.AT_RISK, event.currentIndication());
    }

    @Test
    void noChangeWhenIndicationsMatch() {
        HealthSignal previous = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of(), List.of(), Instant.now());
        HealthSignal current = HealthSignal.restore("hs-2", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-2", List.of(), List.of(), Instant.now());

        Optional<HealthChangeEvent> eventOpt = detector.detectChange(Optional.of(previous), current, Instant.now());

        assertFalse(eventOpt.isPresent());
    }

    @Test
    void requiresCurrentSignal() {
        assertThrows(IllegalArgumentException.class, () -> detector.detectChange(Optional.empty(), null, Instant.now()));
    }
}
