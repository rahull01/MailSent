package com.mailsent.identity.consent.application;

public class ConsentRequiredException extends RuntimeException {
    public ConsentRequiredException(String message) {
        super(message);
    }
}
