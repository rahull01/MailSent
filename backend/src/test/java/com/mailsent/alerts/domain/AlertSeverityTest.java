package com.mailsent.alerts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AlertSeverityTest {

    @Test
    void correctlyParsesKnownTokens() {
        assertEquals(AlertSeverity.CRITICAL, AlertSeverity.fromToken("critical"));
        assertEquals(AlertSeverity.WARNING, AlertSeverity.fromToken("warning"));
        assertEquals(AlertSeverity.INFO, AlertSeverity.fromToken("info"));
    }

    @Test
    void unknownOrMissingTokensAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> AlertSeverity.fromToken(null));
        assertThrows(IllegalArgumentException.class, () -> AlertSeverity.fromToken(""));
        assertThrows(IllegalArgumentException.class, () -> AlertSeverity.fromToken("unknown-level"));
        assertThrows(IllegalArgumentException.class, () -> AlertSeverity.fromToken("CRITICAL")); // Case-sensitive exact match required
    }
}
