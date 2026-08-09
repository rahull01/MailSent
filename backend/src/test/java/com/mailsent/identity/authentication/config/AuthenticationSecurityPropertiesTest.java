package com.mailsent.identity.authentication.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AuthenticationSecurityProperties} — externalized security
 * policies must keep their secure defaults and refuse any configuration that would
 * reduce security below the validated floors.
 */
class AuthenticationSecurityPropertiesTest {

    @Test
    void defaultsAreSecure() {
        AuthenticationSecurityProperties p = new AuthenticationSecurityProperties();
        p.validateFloors(); // must not throw
        assertEquals(12, p.getPasswordMinLength());
        assertEquals(600_000, p.getPbkdf2Iterations());
        assertEquals(16, p.getPbkdf2SaltBytes());
        assertEquals(60, p.getSessionLifetimeMinutes());
    }

    @Test
    void refusesNonPositiveSessionLifetime() {
        AuthenticationSecurityProperties p = new AuthenticationSecurityProperties();
        p.setSessionLifetimeMinutes(0);
        assertThrows(IllegalStateException.class, p::validateFloors);
        p.setSessionLifetimeMinutes(-5);
        assertThrows(IllegalStateException.class, p::validateFloors);
    }

    @Test
    void refusesToLowerPasswordLengthBelowFloor() {
        AuthenticationSecurityProperties p = new AuthenticationSecurityProperties();
        p.setPasswordMinLength(8);
        assertThrows(IllegalStateException.class, p::validateFloors);
    }

    @Test
    void refusesToLowerIterationsBelowFloor() {
        AuthenticationSecurityProperties p = new AuthenticationSecurityProperties();
        p.setPbkdf2Iterations(100_000);
        assertThrows(IllegalStateException.class, p::validateFloors);
    }

    @Test
    void refusesToLowerSaltBelowFloor() {
        AuthenticationSecurityProperties p = new AuthenticationSecurityProperties();
        p.setPbkdf2SaltBytes(8);
        assertThrows(IllegalStateException.class, p::validateFloors);
    }

    @Test
    void acceptsValuesAtOrAboveFloor() {
        AuthenticationSecurityProperties p = new AuthenticationSecurityProperties();
        p.setPasswordMinLength(12);
        p.setPbkdf2Iterations(1_000_000);
        p.setPbkdf2SaltBytes(32);
        p.validateFloors(); // must not throw
        assertEquals(12, p.getPasswordMinLength());
        assertEquals(1_000_000, p.getPbkdf2Iterations());
        assertEquals(32, p.getPbkdf2SaltBytes());
    }
}