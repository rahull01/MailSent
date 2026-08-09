package com.mailsent.identity.authentication.domain;

/**
 * Thrown when an email is absent, malformed, or otherwise unacceptable.
 *
 * <p>A domain-signal of an invalid boundary value; the interfaces layer maps it to a
 * business-first error response per the API error philosophy (meaningful, no
 * implementation leakage).</p>
 */
public class InvalidEmailException extends RuntimeException {

    public InvalidEmailException(String message) {
        super(message);
    }
}