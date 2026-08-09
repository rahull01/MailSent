package com.mailsent.identity.authentication.domain;

/**
 * Thrown when sign-up attempts to create a User whose email is already registered.
 *
 * <p>Email uniqueness is a business rule of the Identity and Access context (one
 * stable identity per person, INV-10; no two entities share one identity). The
 * interfaces layer maps this to a business-first error so a repeat attempt is told
 * the address is already in use, without leaking whether an account exists for any
 * other address.</p>
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}