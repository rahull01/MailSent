package com.mailsent.alerts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AlertTest {

    private static final String ORG = "org-1";

    @Test
    void generatesValidAlertWithAllBusinessLanguageDimensions() {
        String incidentId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Alert alert = Alert.generate(
            ORG,
            incidentId,
            AlertSeverity.CRITICAL,
            "Your email deliverability has dropped to 50%",
            "This severely impacts your ability to reach customers.",
            "Review your DMARC compliance immediately.",
            now
        );

        assertNotNull(alert.id());
        assertEquals(ORG, alert.organizationId());
        assertEquals(incidentId, alert.incidentId());
        assertEquals(AlertSeverity.CRITICAL, alert.severity());
        assertEquals("Your email deliverability has dropped to 50%", alert.whatHappened());
        assertEquals("This severely impacts your ability to reach customers.", alert.whyItMatters());
        assertEquals("Review your DMARC compliance immediately.", alert.actionRequired());
        assertEquals(now, alert.createdAt());
    }

    @Test
    void organizationIdIsTheMandatoryTenantScope() {
        assertThrows(IllegalArgumentException.class, () -> Alert.generate(
            null,
            UUID.randomUUID().toString(),
            AlertSeverity.WARNING,
            "What happened",
            "Why it matters",
            "Action",
            Instant.now()
        ));
        assertThrows(IllegalArgumentException.class, () -> Alert.generate(
            "  ",
            UUID.randomUUID().toString(),
            AlertSeverity.WARNING,
            "What happened",
            "Why it matters",
            "Action",
            Instant.now()
        ));
    }

    @Test
    void incidentIdIsNullableForHealthChangeOriginatedAlerts() {
        Alert alert = Alert.generate(
            ORG,
            null,
            AlertSeverity.INFO,
            "Delivery health recovered",
            "Email is being delivered from a healthy state again.",
            null,
            Instant.now()
        );
        assertNull(alert.incidentId(),
            "a health-change-originated alert must not require a fabricated incident (D2)");
        assertNull(alert.actionRequired());
    }

    @Test
    void rejectsEmptyWhatHappened() {
        assertThrows(IllegalArgumentException.class, () -> Alert.generate(
            ORG,
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
            ORG,
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
            ORG,
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
