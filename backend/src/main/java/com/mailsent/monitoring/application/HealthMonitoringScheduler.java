package com.mailsent.monitoring.application;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.HealthSignalService;

/**
 * HealthMonitoringScheduler — Phase 13 Monitoring (MON-001).
 *
 * <p>Triggers the derivation of health signals for every registered domain
 * on a configurable schedule, monitoring health over time.</p>
 */
@Service
public class HealthMonitoringScheduler {

    private final HealthSignalService healthSignalService;
    private final DomainRepository domains;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public HealthMonitoringScheduler(HealthSignalService healthSignalService, DomainRepository domains) {
        this.healthSignalService = healthSignalService;
        this.domains = domains;
    }

    @Scheduled(fixedDelayString = "${mailsent.monitoring.health.interval:86400000}")
    public void monitorHealth() {
        if (!running.compareAndSet(false, true)) {
            return; // skip if already running
        }
        try {
            for (Domain domain : domains.findAll()) {
                try {
                    healthSignalService.derive(domain.name());
                } catch (Exception e) {
                    // Log and continue to the next domain
                }
            }
        } finally {
            running.set(false);
        }
    }
}
