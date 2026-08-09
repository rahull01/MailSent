package com.mailsent.identity.authentication.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.SessionToken;

/**
 * Unit tests for {@link SecureSessionTokenFactory} — high-entropy, hashed-at-rest
 * session tokens.</p>
 */
class SecureSessionTokenFactoryTest {

    private final SecureSessionTokenFactory factory = new SecureSessionTokenFactory();

    @Test
    void tokenIsHighEntropyAndHashed() {
        SessionToken t = factory.randomToken();
        assertFalse(t.rawToken().isEmpty());
        assertNotEquals(t.rawToken(), t.tokenHash(), "the stored representation must be a one-way hash");
    }

    @Test
    void tokensAreUniquePerCall() {
        SessionToken a = factory.randomToken();
        SessionToken b = factory.randomToken();
        assertNotEquals(a.rawToken(), b.rawToken());
        assertNotEquals(a.tokenHash(), b.tokenHash());
    }

    @Test
    void rawTokenHasExpectedEntropy() {
        // 32 raw bytes -> 64 hex chars (256-bit entropy).
        assertEquals(64, factory.randomToken().rawToken().length());
    }
}