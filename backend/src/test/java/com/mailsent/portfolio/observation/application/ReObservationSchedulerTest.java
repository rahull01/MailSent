package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.mailsent.MailSentApplication;
import com.mailsent.portfolio.observation.config.ObservationScheduleConfig;

/**
 * Unit tests for {@link ReObservationScheduler} (INF-004) — daily default, same-instance
 * non-overlap, and application-layer scheduling (not Monitoring).
 */
class ReObservationSchedulerTest {

    @Test
    void scheduledDelayDefaultsToOneDayInMilliseconds() throws Exception {
        Method method = ReObservationScheduler.class.getDeclaredMethod("reobserve");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        assertEquals("${mailsent.observation.reobserve.interval:86400000}", scheduled.fixedDelayString());
    }

    @Test
    void applicationDoesNotEnableSchedulingGlobally() {
        assertTrue(!MailSentApplication.class.isAnnotationPresent(EnableScheduling.class));
    }

    @Test
    void productionScheduleConfigEnablesSchedulingOutsideTestAndIt() {
        assertTrue(ObservationScheduleConfig.class.isAnnotationPresent(EnableScheduling.class));
        Profile profile = ObservationScheduleConfig.class.getAnnotation(Profile.class);
        assertEquals("!test & !it", profile.value()[0]);
    }

    @Test
    void schedulerLivesInObservationNotMonitoring() {
        assertEquals("com.mailsent.portfolio.observation.application",
            ReObservationScheduler.class.getPackageName());
        assertTrue(ReObservationScheduler.class.getPackageName().startsWith("com.mailsent.portfolio.observation"));
    }

    @Test
    void reentrantCallIsSkipped() throws Exception {
        ReObservationService reObservation = mock(ReObservationService.class);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch hold = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        doAnswer(invocation -> {
            calls.incrementAndGet();
            started.countDown();
            assertTrue(hold.await(5, TimeUnit.SECONDS));
            return null;
        }).when(reObservation).reobserveAll();

        ReObservationScheduler scheduler = new ReObservationScheduler(reObservation);
        Thread first = new Thread(scheduler::reobserve);
        first.start();
        assertTrue(started.await(5, TimeUnit.SECONDS));

        Thread second = new Thread(scheduler::reobserve);
        second.start();
        second.join(2_000);
        assertEquals(1, calls.get());
        verify(reObservation, times(1)).reobserveAll();

        hold.countDown();
        first.join(2_000);
        assertEquals(1, calls.get());
    }

    @Test
    void reobserveDelegatesToService() {
        ReObservationService reObservation = mock(ReObservationService.class);
        new ReObservationScheduler(reObservation).reobserve();
        verify(reObservation).reobserveAll();
    }
}
