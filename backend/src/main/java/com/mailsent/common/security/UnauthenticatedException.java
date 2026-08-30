package com.mailsent.common.security;

/**
 * UnauthenticatedException — raised when a protected resource or operation is
 * accessed without a valid authenticated session.
 */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
