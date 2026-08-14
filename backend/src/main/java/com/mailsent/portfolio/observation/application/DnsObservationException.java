package com.mailsent.portfolio.observation.application;

/**
 * Raised when a DNS observation cannot be performed (INF-001): the domain does not exist
 * (NXDOMAIN), the lookup timed out, or the resolver failed. A failed observation produces no
 * records (INV-01 truth — never a fabricated result).
 */
public class DnsObservationException extends RuntimeException {

    public DnsObservationException(String message) {
        super(message);
    }

    public DnsObservationException(String message, Throwable cause) {
        super(message, cause);
    }
}