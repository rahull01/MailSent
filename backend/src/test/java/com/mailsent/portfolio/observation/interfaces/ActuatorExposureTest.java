package com.mailsent.portfolio.observation.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Actuator exposure for the hermetic test profile: health without details,
 * Prometheus/metrics not included.
 */
@SpringBootTest
@ActiveProfiles("test")
class ActuatorExposureTest {

    @Autowired private Environment environment;

    @Test
    void healthDetailsAreNeverShown() {
        assertEquals("never", environment.getProperty("management.endpoint.health.show-details"));
    }

    @Test
    void prometheusAndMetricsAreNotExposed() {
        String include = environment.getProperty("management.endpoints.web.exposure.include");
        assertEquals("health,info", include);
        assertFalse(include.contains("prometheus"));
        assertFalse(include.contains("metrics"));
    }
}
