package com.mailsent.identity.authentication.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Pbkdf2PasswordHasher} — the security guarantee that a raw
 * password is never stored, and that hashes are salted and verifiable.
 */
class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

    @Test
    void hashedValueIsNotTheRawPassword() {
        String hash = hasher.hash("correct-horse-battery-staple");
        assertFalse(hash.contains("correct-horse-battery-staple"));
        assertTrue(hash.startsWith("pbkdf2$"));
    }

    @Test
    void matchingPasswordVerifies() {
        String hash = hasher.hash("correct-horse-battery-staple");
        assertTrue(hasher.matches("correct-horse-battery-staple", hash));
    }

    @Test
    void wrongPasswordDoesNotVerify() {
        String hash = hasher.hash("correct-horse-battery-staple");
        assertFalse(hasher.matches("another-password-entirely", hash));
    }

    @Test
    void samePasswordProducesDifferentHashEachTime() {
        // Salted: two hashes of the same password must differ.
        String a = hasher.hash("correct-horse-battery-staple");
        String b = hasher.hash("correct-horse-battery-staple");
        assertNotEquals(a, b);
    }

    @Test
    void malformedStoredHashIsRejectedSafely() {
        assertFalse(hasher.matches("whatever", "not-a-valid-hash"));
        assertFalse(hasher.matches("whatever", "pbkdf2$notanumber$00$00"));
    }

    @Test
    void wrongLengthPasswordDoesNotVerify() {
        String hash = hasher.hash("a-password-of-one-other-length");
        assertFalse(hasher.matches("a-password", hash));
    }

    @Test
    void nonHexadecimalHashIsRejectedSafely() {
        assertFalse(hasher.matches("whatever", "pbkdf2$120000$zznothex$1122"));
    }

    @Test
    void truncatedHashIsRejected() {
        assertFalse(hasher.matches("whatever", "pbkdf2"));
    }
}