package com.mailsent.monitoring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.monitoring.domain.Incident;
import com.mailsent.portfolio.observation.domain.HealthIndication;

class IncidentServiceTest {

    private final IncidentRepository incidents = mock(IncidentRepository.class);
    private final IncidentService service = new IncidentService(incidents);

    @Test
    void opensIncidentWhenAtRiskAndNoActiveIncidentExists() {
        HealthChangeEvent event = HealthChangeEvent.detect("dom-1", HealthIndication.HEALTHY, HealthIndication.AT_RISK, Instant.now());
        when(incidents.findActiveByDomainId("dom-1")).thenReturn(Optional.empty());

        service.processHealthChange(event);

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidents).save(captor.capture());
        
        Incident saved = captor.getValue();
        assertEquals("dom-1", saved.domainId());
        assertEquals(event.detectedAt(), saved.openedAt());
    }

    @Test
    void doesNotOpenIncidentWhenAtRiskAndActiveIncidentExists() {
        HealthChangeEvent event = HealthChangeEvent.detect("dom-1", HealthIndication.HEALTHY, HealthIndication.AT_RISK, Instant.now());
        Incident existing = Incident.open("dom-1", Instant.now().minusSeconds(60));
        when(incidents.findActiveByDomainId("dom-1")).thenReturn(Optional.of(existing));

        service.processHealthChange(event);

        verify(incidents, never()).save(any());
    }

    @Test
    void resolvesIncidentWhenHealthyAndActiveIncidentExists() {
        HealthChangeEvent event = HealthChangeEvent.detect("dom-1", HealthIndication.AT_RISK, HealthIndication.HEALTHY, Instant.now());
        Incident existing = Incident.open("dom-1", Instant.now().minusSeconds(60));
        when(incidents.findActiveByDomainId("dom-1")).thenReturn(Optional.of(existing));

        service.processHealthChange(event);

        verify(incidents).save(existing);
        assertTrue(existing.isResolved());
        assertEquals(event.detectedAt(), existing.resolvedAt());
    }

    @Test
    void ignoresHealthyWhenNoActiveIncidentExists() {
        HealthChangeEvent event = HealthChangeEvent.detect("dom-1", HealthIndication.AT_RISK, HealthIndication.HEALTHY, Instant.now());
        when(incidents.findActiveByDomainId("dom-1")).thenReturn(Optional.empty());

        service.processHealthChange(event);

        verify(incidents, never()).save(any());
    }
}
