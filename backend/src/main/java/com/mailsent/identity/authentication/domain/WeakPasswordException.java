package com.mailsent.identity.authentication.domain;

/**
 * Thrown when a password fails the enforced {@link PasswordPolicy}.
 *
 * <p>A domain-signal of a rejected boundary value; the interfaces layer maps it to a
 * business-first error with no implementation leakage.</p>
 */
public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException(String message) {
        super(message);
    }
}