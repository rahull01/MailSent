package com.mailsent.identity.authentication.domain;

/**
 * PasswordPolicy — the domain rule for an acceptable password.
 *
 * <p>Sign-up records nothing about the raw password beyond a hash, so the first line
 * of defense is a minimum strength bound on the value itself. The policy follows
 * current guidance (minimum length; no brittle composition rules that push users to
 * reuse). It is a domain rule: it is applied before any password is hashed or stored.</p>
 *
 * <p>NFR anchoring: input validation (S4), data minimization (S2), and the security
 * principle "validate all inputs / fail closed on doubt."</p>
 */
public final class PasswordPolicy {

    private final int minLength;

    public PasswordPolicy(int minLength) {
        if (minLength < 1) {
            throw new IllegalArgumentException("minLength must be positive");
        }
        this.minLength = minLength;
    }

    /**
     * Validates a candidate password against the policy.
     *
     * @param password the candidate, never {@code null}
     * @throws WeakPasswordException if the password is null, blank, or too short
     */
    public void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new WeakPasswordException("A password is required.");
        }
        if (password.length() < minLength) {
            throw new WeakPasswordException(
                "The password must be at least " + minLength + " characters long.");
        }
    }
}