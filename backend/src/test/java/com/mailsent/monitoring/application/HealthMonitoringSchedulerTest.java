package com.mailsent.monitoring.application;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.HealthSignalService;

class HealthMonitoringSchedulerTest {

    @Test
    void monitorsAllDomains() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);

        Domain d1 = mock(Domain.class);
        when(d1.name()).thenReturn("example.com");
        
        Domain d2 = mock(Domain.class);
        when(d2.name()).thenReturn("example.org");

        when(mockDomains.findAll()).thenReturn(List.of(d1, d2));

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains);

        scheduler.monitorHealth();

        verify(mockSignalService).derive("example.com");
        verify(mockSignalService).derive("example.org");
    }

    @Test
    void continuesOnDomainFailure() {
        HealthSignalService mockSignalService = mock(HealthSignalService.class);
        DomainRepository mockDomains = mock(DomainRepository.class);

        Domain d1 = mock(Domain.class);
        when(d1.name()).thenReturn("error.com");
        
        Domain d2 = mock(Domain.class);
        when(d2.name()).thenReturn("success.com");

        when(mockDomains.findAll()).thenReturn(List.of(d1, d2));

        doThrow(new RuntimeException("Simulated failure")).when(mockSignalService).derive("error.com");

        HealthMonitoringScheduler scheduler = new HealthMonitoringScheduler(mockSignalService, mockDomains);

        scheduler.monitorHealth();

        verify(mockSignalService).derive("error.com");
        verify(mockSignalService).derive("success.com");
    }
}
