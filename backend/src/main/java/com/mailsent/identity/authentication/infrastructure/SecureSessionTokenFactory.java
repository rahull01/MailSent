package com.mailsent.identity.authentication.infrastructure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import com.mailsent.identity.authentication.domain.SessionToken;
import com.mailsent.identity.authentication.domain.SessionTokenFactory;

/**
 * SecureSessionTokenFactory — a hardened session token generator.
 *
 * <p>Produces a raw bearer token from {@link SecureRandom} with high entropy, and a
 * one-way SHA-256 hash of it as the stored representation. Only the hash is ever
 * persisted or compared, so a database leak cannot reveal a usable token. The raw value
 * is returned to the caller once and never logged.</p>
 */
public class SecureSessionTokenFactory implements SessionTokenFactory {

    /** Raw token entropy in bytes (256-bit). */
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random;

    public SecureSessionTokenFactory() {
        this.random = new SecureRandom();
    }

    @Override
    public SessionToken randomToken() {
        byte[] raw = new byte[TOKEN_BYTES];
        random.nextBytes(raw);
        String rawToken = HexFormat.of().formatHex(raw);
        return new SessionToken(rawToken, sha256(raw));
    }

    private String sha256(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}