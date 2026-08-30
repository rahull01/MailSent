package com.mailsent.monitoring.application;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * HealthMonitoringScheduler — Phase 13 Monitoring (MON-001).
 *
 * <p>Triggers the derivation of health signals for every registered domain
 * on a configurable schedule, monitoring health over time and dispatching
 * state changes to {@link HealthChangeDetector}.</p>
 */
@Service
public class HealthMonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthMonitoringScheduler.class);

    private final HealthSignalService healthSignalService;
    private final DomainRepository domains;
    private final Optional<HealthChangeDetector> healthChangeDetector;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    public HealthMonitoringScheduler(HealthSignalService healthSignalService,
                                    DomainRepository domains,
                                    Optional<HealthChangeDetector> healthChangeDetector) {
        this.healthSignalService = healthSignalService;
        this.domains = domains;
        this.healthChangeDetector = healthChangeDetector != null ? healthChangeDetector : Optional.empty();
    }

    public HealthMonitoringScheduler(HealthSignalService healthSignalService, DomainRepository domains) {
        this(healthSignalService, domains, Optional.empty());
    }

    public HealthMonitoringScheduler(HealthSignalService healthSignalService,
                                    DomainRepository domains,
                                    HealthChangeDetector healthChangeDetector) {
        this(healthSignalService, domains, Optional.ofNullable(healthChangeDetector));
    }

    @Scheduled(fixedDelayString = "${mailsent.monitoring.health.interval:86400000}")
    public void monitorHealth() {
        if (!running.compareAndSet(false, true)) {
            log.debug("Health monitoring sweep skipped: previous execution is still running");
            return;
        }
        try {
            for (Domain domain : domains.findAll()) {
                monitorDomain(domain);
            }
        } finally {
            running.set(false);
        }
    }

    private void monitorDomain(Domain domain) {
        try {
            MDC.put("organizationId", domain.organizationId());
            MDC.put("domainId", domain.id());

            // 1. Capture previous latest signal before derivation
            Optional<HealthSignal> previous = latestSignal(domain.id());

            // 2. Derive latest health signal
            Optional<HealthSignal> currentOpt = healthSignalService.derive(domain.name());

            // 3. Dispatch to change detector if a signal was derived and detector is present
            if (currentOpt.isPresent() && healthChangeDetector.isPresent()) {
                HealthSignal current = currentOpt.get();
                healthChangeDetector.get().detectChange(previous, current, current.observedAt());
            }
        } catch (Exception e) {
            log.warn("Health monitoring derivation failed for domain {} (id: {})", domain.name(), domain.id(), e);
        } finally {
            MDC.remove("organizationId");
            MDC.remove("domainId");
        }
    }

    private Optional<HealthSignal> latestSignal(String domainId) {
        return healthSignalService.findByDomainId(domainId).stream()
            .max(Comparator.comparing(HealthSignal::observedAt).thenComparing(HealthSignal::id));
    }

    public boolean isRunning() {
        return running.get();
    }
}
