package com.mailsent.identity.team.domain;

/**
 * Raised when an already-accepted invitation is accepted again (single-use). An invitation
 * grants membership exactly once; the PENDING→ACCEPTED status transition plus the V6
 * {@code uk_invitations_token_hash} backstop enforce this.
 */
public class InvitationAlreadyAcceptedException extends RuntimeException {

    public InvitationAlreadyAcceptedException() {
        super("That invitation has already been accepted.");
    }
}