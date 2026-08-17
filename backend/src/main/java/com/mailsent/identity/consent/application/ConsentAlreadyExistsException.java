package com.mailsent.identity.consent.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConsentAlreadyExistsException extends RuntimeException {
    public ConsentAlreadyExistsException(String message) {
        super(message);
    }
}
