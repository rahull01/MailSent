package com.mailsent.portfolio.observation.application;

/**
 * Raised when an observation is requested for a domain that is not registered in the
 * organization's domain registry (INF-001 authorization gate: only registered domains may
 * be observed).
 */
public class DomainNotRegisteredException extends RuntimeException {

    public DomainNotRegisteredException(String domainName) {
        super("Domain '" + domainName + "' is not registered and cannot be observed.");
    }
}