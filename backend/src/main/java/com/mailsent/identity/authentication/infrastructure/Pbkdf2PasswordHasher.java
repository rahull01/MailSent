package com.mailsent.identity.authentication.infrastructure;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.mailsent.identity.authentication.domain.PasswordHasher;

/**
 * Pbkdf2PasswordHasher — a strong, self-contained password hasher (PBKDF2-HMAC-SHA256).
 *
 * <p>Implements {@link PasswordHasher} using only the JDK, so no cryptographic
 * dependency is introduced. Each hash embeds its own random salt (via
 * {@link SecureRandom}), iteration count, and derived key, encoded as a single
 * {@code pbkdf2$iterations$salt$hash} string. The raw password is never stored or
 * returned; the {@code char[]} copy used for derivation is wiped in a {@code finally}
 * block so it does not linger in this JVM's heap.</p>
 *
 * <p><strong>Security decisions:</strong></p>
 * <ul>
 *   <li>PBKDF2-HMAC-SHA256 is a memory-hard-enough, NIST-sanctioned KDF available
 *       without extra dependencies; a high iteration count slows brute force.</li>
 *   <li>Each hash uses a fresh random 16-byte salt, so identical passwords never hash
 *       the same and rainbow tables are ineffective.</li>
 *   <li>Verification compares derived bytes in constant time (XOR accum., no
 *       short-circuit) to avoid timing side-channels (NFR-S2).</li>
 *   <li>The password is bounded by the {@code PasswordPolicy} before hashing, and the
 *       {@code PBEKeySpec} holds a wiped {@code char[]}, never a retained String.</li>
 * </ul>
 */
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int DEFAULT_ITERATIONS = 120_000;
    private static final int DEFAULT_SALT_BYTES = 16;

    private final SecureRandom random;
    private final int iterations;
    private final int saltBytes;

    public Pbkdf2PasswordHasher() {
        this(DEFAULT_ITERATIONS, DEFAULT_SALT_BYTES);
    }

    public Pbkdf2PasswordHasher(int iterations) {
        this(iterations, DEFAULT_SALT_BYTES);
    }

    public Pbkdf2PasswordHasher(int iterations, int saltBytes) {
        this.random = new SecureRandom();
        this.iterations = iterations;
        this.saltBytes = saltBytes;
    }

    @Override
    public String hash(String rawPassword) {
        byte[] salt = new byte[saltBytes];
        random.nextBytes(salt);
        byte[] derived = derive(rawPassword, salt, iterations);
        return "pbkdf2$" + iterations + "$"
            + HexFormat.of().formatHex(salt) + "$"
            + HexFormat.of().formatHex(derived);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !parts[0].equals("pbkdf2")) {
            return false;
        }
        int its;
        try {
            its = Integer.parseInt(parts[1]);
            byte[] salt = HexFormat.of().parseHex(parts[2]);
            byte[] expected = HexFormat.of().parseHex(parts[3]);
            byte[] actual = derive(rawPassword, salt, its);
            return constantTimeEquals(expected, actual);
        } catch (IllegalArgumentException e) {
            // Any malformed stored hash (non-hex, bad length) fails verification safely:
            // it must never be treated as authentic, nor surface as an unexpected error.
            return false;
        }
    }

    private byte[] derive(String rawPassword, byte[] salt, int its) {
        char[] password = rawPassword.toCharArray();
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, its, KEY_LENGTH_BITS);
            try {
                SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
                return f.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}