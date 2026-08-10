package com.mailsent.identity.team.domain;

/**
 * Raised when acceptance would create a duplicate active membership for the invited user
 * and the target Organization. A user has at most one membership in a given organization;
 * the V6 {@code uk_memberships_user_organization} constraint is the race-safe backstop.
 */
public class MembershipAlreadyExistsException extends RuntimeException {

    public MembershipAlreadyExistsException(String userId, String organizationId) {
        super("The user is already a member of that organization.");
    }
}