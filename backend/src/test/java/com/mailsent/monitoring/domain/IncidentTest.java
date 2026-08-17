package com.mailsent.monitoring.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class IncidentTest {

    @Test
    void opensNewIncident() {
        Instant now = Instant.now();
        Incident incident = Incident.open("dom-1", now);

        assertNotNull(incident.id());
        assertEquals("dom-1", incident.domainId());
        assertEquals(now, incident.openedAt());
        assertNull(incident.resolvedAt());
        assertFalse(incident.isResolved());
    }

    @Test
    void resolvesIncident() {
        Incident incident = Incident.open("dom-1", Instant.now());
        Instant resolvedAt = Instant.now().plusSeconds(60);

        incident.resolve(resolvedAt);

        assertTrue(incident.isResolved());
        assertEquals(resolvedAt, incident.resolvedAt());
    }

    @Test
    void cannotResolveAlreadyResolvedIncident() {
        Incident incident = Incident.open("dom-1", Instant.now());
        incident.resolve(Instant.now().plusSeconds(60));

        assertThrows(IllegalStateException.class, () -> incident.resolve(Instant.now().plusSeconds(120)));
    }
}
