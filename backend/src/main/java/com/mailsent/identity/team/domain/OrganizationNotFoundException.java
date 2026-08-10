package com.mailsent.identity.team.domain;

/**
 * Raised when an invitation is created for (or a membership referenced against) an
 * Organization that does not exist. A clear domain error; the V6 foreign key on
 * {@code memberships.organization_id} and {@code invitations.organization_id} is the
 * final integrity backstop (INV-14).
 */
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(String organizationId) {
        super("No organization exists with id '" + organizationId + "'.");
    }
}