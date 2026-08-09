package com.mailsent.portfolio.domain.domain;

/**
 * Raised by the domain service when a domain is registered under an organization that
 * does not exist. A clear, application-level domain error (ORG-003 decision 3); the
 * database foreign-key constraint on {@code domains.organization_id} remains the final
 * integrity backstop.
 */
public class DomainOrganizationNotFoundException extends RuntimeException {

    public DomainOrganizationNotFoundException(String organizationId) {
        super("No organization exists with id '" + organizationId + "'.");
    }
}