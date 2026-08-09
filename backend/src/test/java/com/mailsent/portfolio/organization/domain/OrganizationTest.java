package com.mailsent.portfolio.organization.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Organization} — the ownership/isolation aggregate (ORG-001).
 */
class OrganizationTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void createProducesStableActiveOrganization() {
        Organization org = Organization.create("Acme Agency", NOW);

        assertTrue(org.id() != null && !org.id().isBlank());
        assertEquals("Acme Agency", org.name());
        assertEquals(Organization.Status.ACTIVE, org.status());
        assertEquals(NOW, org.createdAt());
        assertNull(org.closedAt());
    }

    @Test
    void eachOrganizationGetsItsOwnIdentity() {
        Organization a = Organization.create("A", NOW);
        Organization b = Organization.create("B", NOW);
        assertTrue(!a.id().equals(b.id()), "no two organizations share an identity (INV-10)");
    }

    @Test
    void createRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Organization.create("   ", NOW));
        assertThrows(IllegalArgumentException.class, () -> Organization.create(null, NOW));
    }

    @Test
    void createRejectsNullFormationTime() {
        assertThrows(IllegalArgumentException.class, () -> Organization.create("Acme", null));
    }

    @Test
    void closeEndsLifecycle() {
        Organization org = Organization.create("Acme", NOW);
        org.close(NOW.plusSeconds(100));
        assertEquals(Organization.Status.CLOSED, org.status());
        assertEquals(NOW.plusSeconds(100), org.closedAt());
    }

    @Test
    void closingTwiceIsNoOp() {
        Organization org = Organization.create("Acme", NOW);
        org.close(NOW.plusSeconds(100));
        org.close(NOW.plusSeconds(200));
        assertEquals(NOW.plusSeconds(100), org.closedAt());
    }

    @Test
    void closeRejectsNullClosureTime() {
        Organization org = Organization.create("Acme", NOW);
        assertThrows(IllegalArgumentException.class, () -> org.close(null));
    }

    @Test
    void restoreAdoptsStoredIdentityAndLifecycle() {
        Organization org = Organization.restore(
            "org-1", "Acme", NOW, Organization.Status.CLOSED, NOW.plusSeconds(100));

        assertEquals("org-1", org.id());
        assertEquals("Acme", org.name());
        assertEquals(Organization.Status.CLOSED, org.status());
        assertEquals(NOW.plusSeconds(100), org.closedAt());
    }
}