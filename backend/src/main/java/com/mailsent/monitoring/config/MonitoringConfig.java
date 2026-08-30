package com.mailsent.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.alerts.application.AlertGenerationService;
import com.mailsent.monitoring.application.HealthChangeDetector;
import com.mailsent.monitoring.application.HealthChangeEventRepository;
import com.mailsent.monitoring.application.IncidentRepository;
import com.mailsent.monitoring.application.IncidentService;

/**
 * MonitoringConfig — production wiring for the health change-detection pipeline
 * (MON-002/003/004) and its ALERT-002 generation seam.
 *
 * <p>{@link HealthChangeDetector} and {@link IncidentService} are plain application
 * classes (constructed manually in unit tests), so they must be registered here for the
 * scheduled {@code HealthMonitoringScheduler} to dispatch derived signals to them. The
 * beans are active only outside the hermetic {@code test} profile, mirroring the
 * persistence adapters they depend on ({@code @Profile("!test")}); under {@code test}
 * the scheduler's {@code Optional<HealthChangeDetector>} stays empty by design.</p>
 */
@Configuration
public class MonitoringConfig {

    @Bean
    @Profile("!test")
    public AlertGenerationService alertGenerationService() {
        return new AlertGenerationService();
    }

    @Bean
    @Profile("!test")
    public IncidentService incidentService(IncidentRepository incidents) {
        return new IncidentService(incidents);
    }

    @Bean
    @Profile("!test")
    public HealthChangeDetector healthChangeDetector(HealthChangeEventRepository events,
                                                     IncidentService incidentService,
                                                     AlertGenerationService alertGenerationService) {
        return new HealthChangeDetector(events, incidentService, alertGenerationService);
    }
}
