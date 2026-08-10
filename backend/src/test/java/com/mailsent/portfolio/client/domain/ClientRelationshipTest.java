package com.mailsent.portfolio.client.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link ClientRelationship} aggregate (CLIENT-001) — required fields,
 * ACTIVE formation with {@code endedAt = null}, and the irreversible ACTIVE → ENDED lifecycle.
 */
class ClientRelationshipTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String AGENCY = "org-agency";
    private static final String CLIENT = "org-client";

    @Test
    void formCreatesActiveRelationshipWithNullEndedAt() {
        ClientRelationship relationship = ClientRelationship.form(AGENCY, CLIENT, NOW);
        assertEquals(ClientRelationship.Status.ACTIVE, relationship.status());
        assertFalse(relationship.status() == ClientRelationship.Status.ENDED);
        assertEquals(AGENCY, relationship.agencyOrganizationId());
        assertEquals(CLIENT, relationship.clientOrganizationId());
        assertEquals(NOW, relationship.formedAt());
        assertNull(relationship.endedAt(), "endedAt is null while ACTIVE");
        assertTrue(relationship.id() != null && !relationship.id().isBlank());
    }

    @Test
    void formRequiresAgencyOrganization() {
        assertThrows(IllegalArgumentException.class,
            () -> ClientRelationship.form(null, CLIENT, NOW));
    }

    @Test
    void formRequiresClientOrganization() {
        assertThrows(IllegalArgumentException.class,
            () -> ClientRelationship.form(AGENCY, null, NOW));
    }

    @Test
    void formRequiresFormationTime() {
        assertThrows(IllegalArgumentException.class,
            () -> ClientRelationship.form(AGENCY, CLIENT, null));
    }

    @Test
    void formRejectsSelfRelationship() {
        assertThrows(IllegalArgumentException.class,
            () -> ClientRelationship.form(AGENCY, AGENCY, NOW),
            "an organization cannot be its own client");
    }

    @Test
    void endTransitionsActiveToEndedAndRecordsEndedAt() {
        ClientRelationship relationship = ClientRelationship.form(AGENCY, CLIENT, NOW);
        relationship.end(NOW.plusSeconds(60));
        assertEquals(ClientRelationship.Status.ENDED, relationship.status());
        assertEquals(NOW.plusSeconds(60), relationship.endedAt());
    }

    @Test
    void endIsIrreversibleAndNoOpOnceEnded() {
        ClientRelationship relationship = ClientRelationship.form(AGENCY, CLIENT, NOW);
        relationship.end(NOW.plusSeconds(60));
        relationship.end(NOW.plusSeconds(120));
        assertEquals(NOW.plusSeconds(60), relationship.endedAt(), "endedAt is unchanged after the first end");
    }

    @Test
    void restoreAdoptsStoredStateVerbatim() {
        ClientRelationship relationship = ClientRelationship.restore("rel-1", AGENCY, CLIENT, NOW,
            ClientRelationship.Status.ENDED, NOW.plusSeconds(9));
        assertEquals("rel-1", relationship.id());
        assertEquals(ClientRelationship.Status.ENDED, relationship.status());
        assertEquals(NOW.plusSeconds(9), relationship.endedAt());
    }
}