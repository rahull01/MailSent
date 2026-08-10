package com.mailsent.identity.team.domain;

/**
 * Raised on acceptance when the invitation's {@code organizationId} does not equal the
 * invitee's immutable home Organization ({@code user.organizationId}). TEAM-001 creates no
 * membership and never reassigns a home organization, so acceptance is only permitted when
 * the invited organization IS the user's home organization (frozen contract v8 §8 step 7).
 */
public class OrganizationMismatchException extends RuntimeException {

    public OrganizationMismatchException() {
        super("This invitation is for a different organization than your home organization.");
    }
}