package com.mailsent.portfolio.observation.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReObservationScheduler;
import com.mailsent.portfolio.observation.application.ReObservationService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;

/**
 * Confirms INF-004 scheduling is registered outside {@code test}/{@code it}
 * and is absent in those profiles (service remains available).
 */
class ObservationScheduleConfigTest {

    @Test
    void localProfileRegistersSchedulerAndEnableScheduling() {
        try (AnnotationConfigApplicationContext ctx = contextWithProfiles("local")) {
            assertEquals(1, ctx.getBeanNamesForType(ReObservationScheduler.class).length);
            assertEquals(1, ctx.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class).length);
            assertEquals(1, ctx.getBeanNamesForType(ReObservationService.class).length);
        }
    }

    @Test
    void prodProfileRegistersSchedulerAndEnableScheduling() {
        try (AnnotationConfigApplicationContext ctx = contextWithProfiles("prod")) {
            assertEquals(1, ctx.getBeanNamesForType(ReObservationScheduler.class).length);
            assertEquals(1, ctx.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class).length);
        }
    }

    @Test
    void testProfileDoesNotCreateSchedulerOrEnableScheduling() {
        try (AnnotationConfigApplicationContext ctx = contextWithProfiles("test")) {
            assertEquals(0, ctx.getBeanNamesForType(ReObservationScheduler.class).length);
            assertEquals(0, ctx.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class).length);
            assertEquals(1, ctx.getBeanNamesForType(ReObservationService.class).length);
        }
    }

    @Test
    void itProfileDoesNotCreateSchedulerOrEnableScheduling() {
        try (AnnotationConfigApplicationContext ctx = contextWithProfiles("it")) {
            assertEquals(0, ctx.getBeanNamesForType(ReObservationScheduler.class).length);
            assertEquals(0, ctx.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class).length);
            assertEquals(1, ctx.getBeanNamesForType(ReObservationService.class).length);
        }
    }

    private static AnnotationConfigApplicationContext contextWithProfiles(String... profiles) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles(profiles);
        ctx.register(ObservationScheduleConfig.class, ReObservationConfig.class, Stubs.class);
        ctx.refresh();
        return ctx;
    }

    @Configuration
    static class Stubs {
        @Bean
        DomainRepository domainRepository() {
            DomainRepository domains = mock(DomainRepository.class);
            when(domains.findAll()).thenReturn(List.of());
            return domains;
        }

        @Bean
        ObservationEvidenceService observationEvidenceService() {
            return mock(ObservationEvidenceService.class);
        }

        @Bean
        ReputationObservationService reputationObservationService() {
            return mock(ReputationObservationService.class);
        }

        @Bean
        com.mailsent.identity.consent.application.ConsentService consentService() {
            return mock(com.mailsent.identity.consent.application.ConsentService.class);
        }
    }
}
