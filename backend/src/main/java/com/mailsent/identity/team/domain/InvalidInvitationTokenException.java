package com.mailsent.identity.team.domain;

/**
 * Raised when an acceptance token does not resolve to any invitation. For a malformed or
 * unknown token nothing is revealed (no existence oracle for invitations).
 */
public class InvalidInvitationTokenException extends RuntimeException {

    public InvalidInvitationTokenException() {
        super("That invitation token is not valid.");
    }
}