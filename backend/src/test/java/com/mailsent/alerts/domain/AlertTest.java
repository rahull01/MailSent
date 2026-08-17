package com.mailsent.alerts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AlertTest {

    @Test
    void generatesValidAlertWithAllBusinessLanguageDimensions() {
        String incidentId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Alert alert = Alert.generate(
            incidentId,
            AlertSeverity.CRITICAL,
            "Your email deliverability has dropped to 50%",
            "This severely impacts your ability to reach customers.",
            "Review your DMARC compliance immediately.",
            now
        );

        assertNotNull(alert.id());
        assertEquals(incidentId, alert.incidentId());
        assertEquals(AlertSeverity.CRITICAL, alert.severity());
        assertEquals("Your email deliverability has dropped to 50%", alert.whatHappened());
        assertEquals("This severely impacts your ability to reach customers.", alert.whyItMatters());
        assertEquals("Review your DMARC compliance immediately.", alert.actionRequired());
        assertEquals(now, alert.createdAt());
    }

    @Test
    void rejectsEmptyWhatHappened() {
        assertThrows(IllegalArgumentException.class, () -> Alert.generate(
            UUID.randomUUID().toString(),
            AlertSeverity.WARNING,
            " ",
            "Why it matters",
            "Action",
            Instant.now()
        ));
    }

    @Test
    void rejectsEmptyWhyItMatters() {
        assertThrows(IllegalArgumentException.class, () -> Alert.generate(
            UUID.randomUUID().toString(),
            AlertSeverity.INFO,
            "What happened",
            "",
            "Action",
            Instant.now()
        ));
    }

    @Test
    void permitsNullActionRequired() {
        Alert alert = Alert.generate(
            UUID.randomUUID().toString(),
            AlertSeverity.INFO,
            "What happened",
            "Why it matters",
            null,
            Instant.now()
        );
        assertEquals(AlertSeverity.INFO, alert.severity());
    }
}
