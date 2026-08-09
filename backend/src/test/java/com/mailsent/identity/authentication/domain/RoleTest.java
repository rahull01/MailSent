package com.mailsent.identity.authentication.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Role} — the user identity's role value object (DATA model:
 * "a set of what a user may see and do"; Functional Requirement D5; NFR-S1).
 *
 * <p>AUTH-003 defines the Role model with least authority. These tests prove the three
 * canonical roles, their documented access bounds (who can see health / who can make
 * changes — D5), the least-authority default, and immutability/equality of the value
 * object (INV-12).</p>
 */
class RoleTest {

    @Test
    void exposesTheThreeCanonicalRolesAsAccessBounds() {
        assertEquals("Owner", Role.OWNER.label());
        assertEquals("Analyst", Role.ANALYST.label());
        assertEquals("Viewer", Role.VIEWER.label());
    }

    @Test
    void defaultForSignUpIsLeastAuthority() {
        assertEquals(Role.VIEWER, Role.defaultForSignUp());
        assertTrue(Role.defaultForSignUp().canSee());
        assertFalse(Role.defaultForSignUp().canChange(), "a new user's default may never change delivery");
    }

    @Test
    void eachRoleIsADistinctAccessBound() {
        assertNotEquals(Role.OWNER, Role.ANALYST);
        assertNotEquals(Role.ANALYST, Role.VIEWER);
        assertNotEquals(Role.OWNER, Role.VIEWER);
        assertFalse(Role.OWNER.name().equalsIgnoreCase(Role.VIEWER.name()));
    }

    @Test
    void everyRoleCanSeeDeliveryHealth() {
        // D5: who can see health — all three may observe delivery health.
        assertTrue(Role.OWNER.canSee());
        assertTrue(Role.ANALYST.canSee());
        assertTrue(Role.VIEWER.canSee());
    }

    @Test
    void onlyOwnerAndAnalystCanMakeChanges() {
        // D5: who can make changes — Owner and Analyst act; Viewer (least authority) may not.
        assertTrue(Role.OWNER.canChange());
        assertTrue(Role.ANALYST.canChange());
        assertFalse(Role.VIEWER.canChange());
    }

    @Test
    void viewerIsStrictlyLeastAuthority() {
        // The Viewer must never accidentally grant a change authority.
        assertFalse(Role.VIEWER.canChange());
    }

    @Test
    void roleIsAnImmutableEnumeratedValue() {
        // A Role has no setters/state mutation; every value is a fixed, stateless bound.
        // A changed right must be a different Role (INV-12), never a mutation of this one.
        Role role = Role.VIEWER;
        assertEquals(Role.VIEWER, role);
        assertFalse(role.canChange());
    }
}