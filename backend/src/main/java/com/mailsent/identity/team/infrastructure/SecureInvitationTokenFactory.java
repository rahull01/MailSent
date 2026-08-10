package com.mailsent.identity.team.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import com.mailsent.identity.team.domain.InvitationToken;
import com.mailsent.identity.team.domain.InvitationTokenFactory;

/**
 * SecureInvitationTokenFactory — a hardened invitation token generator.
 *
 * <p>Produces a raw acceptance token from {@link SecureRandom} with high entropy and a
 * one-way SHA-256 hash of it as the stored representation, mirroring
 * {@code SecureSessionTokenFactory}. Only the hash is ever persisted or compared, so a
 * database leak cannot reveal a usable invitation token. The raw value is returned to the
 * issuer once and never logged.</p>
 */
public class SecureInvitationTokenFactory implements InvitationTokenFactory {

    /** Raw token entropy in bytes (256-bit). */
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random;

    public SecureInvitationTokenFactory() {
        this.random = new SecureRandom();
    }

    @Override
    public InvitationToken randomToken() {
        byte[] raw = new byte[TOKEN_BYTES];
        random.nextBytes(raw);
        String rawToken = HexFormat.of().formatHex(raw);
        // The stored hash is the SHA-256 of the raw token STRING, so {@link #hash(String)}
        // re-derives the identical hash when a presented raw token is looked up on accept.
        return new InvitationToken(rawToken, sha256(rawToken.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String hash(String rawToken) {
        return sha256(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}