package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activates Spring {@code @Scheduled} processing for INF-004 in production
 * profiles. Excluded from {@code test} and {@code it} so scheduled re-observation
 * never registers during unit or integration tests.
 *
 * <p>{@code spring.task.scheduling.enabled=false} is not sufficient on its own:
 * {@link EnableScheduling} still registers {@code ScheduledAnnotationBeanPostProcessor}
 * independently of Boot's task-scheduling auto-configuration.</p>
 */
@Configuration
@EnableScheduling
@Profile("!test & !it")
public class ObservationScheduleConfig {
}
