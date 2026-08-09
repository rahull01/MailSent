package com.mailsent.identity.authentication.domain;

/**
 * Thrown by sign-in when the offered credentials do not authenticate.
 *
 * <p><strong>Anti-enumeration.</strong> The same error is raised whether the email is
 * unknown or the password is wrong, and the message never reveals which. A caller cannot
 * infer from the response whether an email is registered — closing the classic user
 * enumeration (account-oracle) vector (NFR-S1, security principle "never leak").</p>
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}