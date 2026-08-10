package com.mailsent.portfolio.client.domain;

/**
 * Raised by {@code ClientRelationshipService.form} when either referenced Organization does
 * not exist. CLIENT-001 links two EXISTING organizations and never creates one; both foreign
 * keys in V7 are the final integrity backstop (INV-14).
 */
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(String organizationId) {
        super("No organization exists with id '" + organizationId + "'.");
    }
}