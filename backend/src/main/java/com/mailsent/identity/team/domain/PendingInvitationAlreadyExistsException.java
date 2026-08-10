package com.mailsent.identity.team.domain;

/**
 * Raised when an invitation would create a duplicate PENDING invitation for the exact pair
 * {@code (organizationId, canonical inviteeEmail)}. Prevented race-safely by the PostgreSQL
 * PENDING-scoped partial unique index; a persistence violation on that index is translated
 * here (frozen contract v8 §5).
 */
public class PendingInvitationAlreadyExistsException extends RuntimeException {

    public PendingInvitationAlreadyExistsException(String organizationId, String inviteeEmail) {
        super("A pending invitation already exists for " + inviteeEmail + " in that organization.");
    }
}