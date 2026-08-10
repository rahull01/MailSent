package com.mailsent.identity.team.domain;

/**
 * Raised on acceptance when the presented token is not exactly 64 lowercase ASCII
 * hexadecimal characters ({@code [0-9a-f]}). A malformed token is rejected BEFORE any
 * database lookup and reveals nothing about whether an invitation exists
 * (frozen contract v8 §4 input validation).
 */
public class MalformedInvitationTokenException extends RuntimeException {

    public MalformedInvitationTokenException() {
        super("That invitation token is not valid.");
    }
}