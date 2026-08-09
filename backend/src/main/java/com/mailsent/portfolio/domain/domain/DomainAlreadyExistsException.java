package com.mailsent.portfolio.domain.domain;

/**
 * Raised when a domain name that already exists is registered again. A domain name is
 * globally unique (ORG-003 decision 1); the {@code uk_domains_name} database constraint
 * is the race-safe final backstop, while this application-level check yields the clear
 * domain error first.
 */
public class DomainAlreadyExistsException extends RuntimeException {

    public DomainAlreadyExistsException(String name) {
        super("A domain named '" + name + "' is already registered.");
    }
}