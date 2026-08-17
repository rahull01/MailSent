package com.mailsent.portfolio.observation.application;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Triggers {@link ReObservationService#reobserveAll()} on a configurable fixed
 * delay (default one day). This is Delivery Observation (INF-004), not
 * Monitoring (MON-001) and not Health Signal (HEALTH-001).
 *
 * <p>Non-overlap on one instance: Spring Boot's scheduling pool size is set to
 * 1 ({@code spring.task.scheduling.pool.size}), so a second {@code @Scheduled}
 * invocation waits until this method returns. {@link AtomicBoolean} additionally
 * skips a reentrant call on the same instance.</p>
 */
public class ReObservationScheduler {

    private final ReObservationService reObservation;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ReObservationScheduler(ReObservationService reObservation) {
        this.reObservation = reObservation;
    }

    @Scheduled(fixedDelayString = "${mailsent.observation.reobserve.interval:86400000}")
    public void reobserve() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            reObservation.reobserveAll();
        } finally {
            running.set(false);
        }
    }
}
