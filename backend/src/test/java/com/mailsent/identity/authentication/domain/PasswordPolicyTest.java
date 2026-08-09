package com.mailsent.identity.authentication.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PasswordPolicy} — the enforced minimum-strength rule applied
 * before any password is hashed or stored.</p>
 */
class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy(12);

    @Test
    void acceptsAPasswordOfSufficientLength() {
        assertDoesNotThrow(() -> policy.validate("a-passphrase-that-is-long-enough-123"));
    }

    @Test
    void rejectsNull() {
        assertThrows(WeakPasswordException.class, () -> policy.validate(null));
    }

    @Test
    void rejectsBlank() {
        assertThrows(WeakPasswordException.class, () -> policy.validate("   "));
    }

    @Test
    void rejectsTooShort() {
        assertThrows(WeakPasswordException.class, () -> policy.validate("shortpass"));
        assertThrows(WeakPasswordException.class, () -> policy.validate("12345678"));
    }

    @Test
    void rejectsExactlyBelowBoundary() {
        assertThrows(WeakPasswordException.class, () -> policy.validate("a".repeat(11)));
    }
}