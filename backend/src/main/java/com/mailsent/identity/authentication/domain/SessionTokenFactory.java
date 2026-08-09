package com.mailsent.identity.authentication.domain;

/**
 * SessionTokenFactory — the port for producing a hardened session token.
 *
 * <p>Placed in the domain so the security rule ("a session token is high-entropy and is
 * stored only as a hash") is a business contract. An infrastructure adapter satisfies it
 * using {@code SecureRandom} for the raw value and a one-way hash (e.g. SHA-256) for the
 * stored representation. Supports NFR-S2 and the Constitution's secrets rule.</p>
 */
public interface SessionTokenFactory {

    /**
     * Creates a fresh, unpredictable token with its stored hash representation.
     */
    SessionToken randomToken();
}