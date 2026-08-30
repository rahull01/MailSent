package com.mailsent.monitoring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mailsent.alerts.application.AlertGenerationService;
import com.mailsent.alerts.domain.Alert;
import com.mailsent.alerts.domain.AlertSeverity;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class HealthChangeDetectorTest {

    private final HealthChangeEventRepository mockRepo = Mockito.mock(HealthChangeEventRepository.class);
    private final IncidentService mockIncidents = Mockito.mock(IncidentService.class);
    private final AlertGenerationService spyAlerts = Mockito.spy(new AlertGenerationService());
    private final HealthChangeDetector detector = new HealthChangeDetector(mockRepo, mockIncidents, spyAlerts);

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

        Mockito.verify(mockRepo).save(event);
        // ALERT-002 seam (D4): the detector hands every realized change to the generator.
        Mockito.verify(spyAlerts).generate(event);
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

        Mockito.verify(mockRepo).save(event);
        Mockito.verify(spyAlerts).generate(event);
    }

    @Test
    void noChangeWhenIndicationsMatch() {
        HealthSignal previous = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of(), List.of(), Instant.now());
        HealthSignal current = HealthSignal.restore("hs-2", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-2", List.of(), List.of(), Instant.now());

        Optional<HealthChangeEvent> eventOpt = detector.detectChange(Optional.of(previous), current, Instant.now());

        assertFalse(eventOpt.isPresent());

        Mockito.verifyNoInteractions(mockRepo);
        Mockito.verifyNoInteractions(spyAlerts);
    }

    @Test
    void firstObservationOfHealthyDomainGeneratesNoAlertButStillPersistsEvent() {
        HealthSignal current = HealthSignal.restore("hs-2", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-2", List.of(), List.of(), Instant.now());

        Optional<HealthChangeEvent> eventOpt = detector.detectChange(Optional.empty(), current, Instant.now());

        assertTrue(eventOpt.isPresent());
        Mockito.verify(mockRepo).save(eventOpt.get());
        Mockito.verify(spyAlerts).generate(eventOpt.get());

        // The generator itself returns empty for a healthy first observation (D1 policy),
        // proven fully in AlertGenerationServiceTest; here we prove the wired seam yields
        // no alert of its own for this case.
        assertNull(new AlertGenerationService().generate(eventOpt.get()).orElse(null));
    }

    @Test
    void wiredGeneratorProducesCriticalForFirstAtRiskObservation() {
        HealthSignal current = HealthSignal.restore("hs-9", "org-9", "dom-9", "example.com",
            HealthIndication.AT_RISK, "oe-9", List.of(), List.of(), Instant.now());

        Optional<HealthChangeEvent> eventOpt = detector.detectChange(Optional.empty(), current, Instant.now());

        assertTrue(eventOpt.isPresent());
        Alert alert = spyAlerts.generate(eventOpt.get()).orElseThrow();
        assertEquals("org-9", alert.organizationId(), "tenant scope propagates from the event verbatim");
        assertEquals(AlertSeverity.CRITICAL, alert.severity());
        Mockito.verify(mockRepo).save(any(HealthChangeEvent.class));
    }

    @Test
    void requiresCurrentSignal() {
        assertThrows(IllegalArgumentException.class, () -> detector.detectChange(Optional.empty(), null, Instant.now()));
    }
}
