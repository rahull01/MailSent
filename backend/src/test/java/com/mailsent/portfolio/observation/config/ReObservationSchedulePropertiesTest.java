package com.mailsent.portfolio.observation.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Confirms INF-004 scheduling defaults: one daily execution and a single-thread
 * pool so {@code @Scheduled} methods on this instance do not overlap.
 */
class ReObservationSchedulePropertiesTest {

    @Test
    void applicationYamlDefaultsDailyIntervalAndSingleThreadPool() throws IOException {
        String yaml = read("/application.yml");
        assertTrue(yaml.contains("interval: 86400000"), "default interval is one day in milliseconds");
        assertTrue(yaml.contains("size: 1"), "scheduling pool size is 1 (Spring-native non-overlap)");
    }

    @Test
    void testAndItProfilesDisableSchedulingSoJobsDoNotFireInTests() throws IOException {
        String testYaml = read("/application-test.yml");
        String itYaml = read("/application-it.yml");
        assertTrue(testYaml.contains("enabled: false"));
        assertTrue(itYaml.contains("enabled: false"));
    }

    private static String read(String classpath) throws IOException {
        try (InputStream in = ReObservationSchedulePropertiesTest.class.getResourceAsStream(classpath)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
