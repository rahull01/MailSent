package com.mailsent.alerts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AlertPreferenceTest {

    @Test
    void configuresValidDigestPreference() {
        String userId = UUID.randomUUID().toString();
        
        AlertPreference preference = AlertPreference.configure(
            userId, 
            AlertSeverity.WARNING, 
            AlertPreference.DeliveryMode.DIGEST, 
            AlertChannel.EMAIL
        );
        
        assertEquals(userId, preference.userId());
        assertEquals(AlertSeverity.WARNING, preference.minimumSeverity());
        assertEquals(AlertPreference.DeliveryMode.DIGEST, preference.deliveryMode());
        assertEquals(AlertChannel.EMAIL, preference.channel());
    }

    @Test
    void rejectsImmediateOrDigestModeWithoutChannel() {
        String userId = UUID.randomUUID().toString();
        
        assertThrows(IllegalArgumentException.class, () -> 
            AlertPreference.configure(userId, AlertSeverity.CRITICAL, AlertPreference.DeliveryMode.IMMEDIATE, null)
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            AlertPreference.configure(userId, AlertSeverity.WARNING, AlertPreference.DeliveryMode.DIGEST, null)
        );
    }

    @Test
    void createsMuteAllPreferenceCorrectly() {
        String userId = UUID.randomUUID().toString();
        
        AlertPreference muted = AlertPreference.muteAll(userId);
        
        assertEquals(userId, muted.userId());
        assertEquals(AlertPreference.DeliveryMode.MUTED, muted.deliveryMode());
        assertNull(muted.channel());
    }
}
