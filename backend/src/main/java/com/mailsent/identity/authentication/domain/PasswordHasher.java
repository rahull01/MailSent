package com.mailsent.identity.authentication.domain;

/**
 * PasswordHasher — the port for turning a raw password into a stored, irreversible
 * representation and for verifying against it.
 *
 * <p>Placed in the domain so the sign-up rule ("never store a password in plain
 * text") is expressed as a business contract, satisfied by an infrastructure adapter
 * using a strong, salted, computationally expensive algorithm. Supports NFR-S2
 * (confidentiality and integrity of customer data) and the Constitution security
 * principles.</p>
 */
public interface PasswordHasher {

    /**
     * Encodes a raw password into a self-contained, irreversible string.
     * The encoded value must embed its own salt and parameterisation so it can be
     * verified later without side data.
     */
    String hash(String rawPassword);

    /**
     * Confirms a raw password against a previously encoded value in constant time.
     *
     * @return {@code true} when {@code rawPassword} matches {@code storedHash}
     */
    boolean matches(String rawPassword, String storedHash);
}