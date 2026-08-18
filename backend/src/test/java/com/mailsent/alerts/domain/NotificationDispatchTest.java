package com.mailsent.alerts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class NotificationDispatchTest {

    @Test
    void createsAndDispatchesNotification() {
        Instant now = Instant.now();
        NotificationMessage msg = NotificationMessage.create(
                "user-1",
                "admin@client.com",
                AlertChannel.EMAIL,
                "Critical SPF Failure",
                "SPF lookups exceeded limit.",
                now
        );

        assertNotNull(msg.id());
        assertEquals(NotificationMessage.DispatchStatus.PENDING, msg.status());
        assertEquals("user-1", msg.recipientUserId());
        assertEquals("admin@client.com", msg.recipientAddress());

        msg.markSent(now.plusSeconds(2));
        assertEquals(NotificationMessage.DispatchStatus.SENT, msg.status());
        assertEquals(now.plusSeconds(2), msg.sentAt());
    }

    @Test
    void buildsDigestNotification() {
        Alert a1 = Alert.generate("inc-1", AlertSeverity.CRITICAL, "SPF Limit Exceeded", "Promotional mail sent to spam", "Flatten SPF", Instant.now());
        Alert a2 = Alert.generate("inc-2", AlertSeverity.WARNING, "DMARC Quarantine Recommended", "Impersonation risk", "Enforce p=quarantine", Instant.now());

        NotificationMessage digest = DigestBuilder.buildDigest(
                "user-1",
                "digest@client.com",
                AlertChannel.EMAIL,
                List.of(a1, a2),
                Instant.now()
        );

        assertNotNull(digest.id());
        assertTrue(digest.subject().contains("1 Critical"));
        assertTrue(digest.body().contains("SPF Limit Exceeded"));
        assertTrue(digest.body().contains("DMARC Quarantine Recommended"));
    }

    @Test
    void rejectsEmptyAlertsForDigest() {
        assertThrows(IllegalArgumentException.class, () ->
                DigestBuilder.buildDigest("user-1", "test@test.com", AlertChannel.EMAIL, List.of(), Instant.now())
        );
    }
}
