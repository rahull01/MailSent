package com.mailsent.monitoring.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class HealthMonitoringSchedulerTest {

    @Test
    void monitorsAllDomains() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);

        Domain d1 = mock(Domain.class);
        when(d1.name()).thenReturn("example.com");
        when(d1.id()).thenReturn("dom-1");
        when(d1.organizationId()).thenReturn("org-1");

        Domain d2 = mock(Domain.class);
        when(d2.name()).thenReturn("example.org");
        when(d2.id()).thenReturn("dom-2");
        when(d2.organizationId()).thenReturn("org-1");

        when(mockDomains.findAll()).thenReturn(List.of(d1, d2));

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains);

        scheduler.monitorHealth();

        verify(mockSignalService).derive("example.com");
        verify(mockSignalService).derive("example.org");
        assertFalse(scheduler.isRunning());
    }

    @Test
    void continuesOnDomainFailure() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);

        Domain d1 = mock(Domain.class);
        when(d1.name()).thenReturn("error.com");
        when(d1.id()).thenReturn("dom-1");
        when(d1.organizationId()).thenReturn("org-1");

        Domain d2 = mock(Domain.class);
        when(d2.name()).thenReturn("success.com");
        when(d2.id()).thenReturn("dom-2");
        when(d2.organizationId()).thenReturn("org-1");

        when(mockDomains.findAll()).thenReturn(List.of(d1, d2));

        doThrow(new RuntimeException("Simulated failure")).when(mockSignalService).derive("error.com");

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains);

        scheduler.monitorHealth();

        verify(mockSignalService).derive("error.com");
        verify(mockSignalService).derive("success.com");
        assertFalse(scheduler.isRunning());
    }

    @Test
    void dispatchesToHealthChangeDetectorWhenSignalDerived() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);
        HealthChangeDetector mockDetector = mock(HealthChangeDetector.class);

        Domain domain = mock(Domain.class);
        when(domain.name()).thenReturn("monitored.com");
        when(domain.id()).thenReturn("dom-1");
        when(domain.organizationId()).thenReturn("org-1");

        when(mockDomains.findAll()).thenReturn(List.of(domain));

        Instant prevTime = Instant.parse("2026-08-20T10:00:00Z");
        Instant currTime = Instant.parse("2026-08-22T10:00:00Z");

        HealthSignal prevSignal = mock(HealthSignal.class);
        when(prevSignal.id()).thenReturn("sig-prev");
        when(prevSignal.observedAt()).thenReturn(prevTime);
        when(prevSignal.indication()).thenReturn(HealthIndication.HEALTHY);

        HealthSignal currSignal = mock(HealthSignal.class);
        when(currSignal.id()).thenReturn("sig-curr");
        when(currSignal.observedAt()).thenReturn(currTime);
        when(currSignal.indication()).thenReturn(HealthIndication.AT_RISK);

        when(mockSignalService.findByDomainId("dom-1")).thenReturn(List.of(prevSignal));
        when(mockSignalService.derive("monitored.com")).thenReturn(Optional.of(currSignal));

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains, mockDetector);

        scheduler.monitorHealth();

        verify(mockDetector).detectChange(eq(Optional.of(prevSignal)), eq(currSignal), eq(currTime));
    }

    @Test
    void doesNotDispatchToDetectorWhenNoSignalDerived() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);
        HealthChangeDetector mockDetector = mock(HealthChangeDetector.class);

        Domain domain = mock(Domain.class);
        when(domain.name()).thenReturn("unchanged.com");
        when(domain.id()).thenReturn("dom-2");
        when(domain.organizationId()).thenReturn("org-1");

        when(mockDomains.findAll()).thenReturn(List.of(domain));
        when(mockSignalService.derive("unchanged.com")).thenReturn(Optional.empty());

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains, mockDetector);

        scheduler.monitorHealth();

        verify(mockDetector, never()).detectChange(any(), any(), any());
    }

    @Test
    void cleansUpMdcContextAfterExecution() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);

        Domain domain = mock(Domain.class);
        when(domain.name()).thenReturn("mdc.com");
        when(domain.id()).thenReturn("dom-3");
        when(domain.organizationId()).thenReturn("org-test");

        when(mockDomains.findAll()).thenReturn(List.of(domain));

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains);

        scheduler.monitorHealth();

        assertNull(MDC.get("organizationId"));
        assertNull(MDC.get("domainId"));
    }
}
