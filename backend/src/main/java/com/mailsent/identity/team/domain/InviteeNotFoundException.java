package com.mailsent.identity.team.domain;

/**
 * Raised when an invitation is accepted but the invited email has no existing account.
 *
 * <p>TEAM-001 deliberately does not invent account creation on acceptance (decision 9);
 * the new-user path (accepting an invitation requires signup) is a separate, explicitly
 * approved decision. Until then, the acceptance operation refuses with this clear domain
 * error rather than silently redesigning the signup flow.</p>
 *
 * <p>The message is deliberately generic: the acceptance endpoint is token-gated public
 * surface, and the frozen contract forbids revealing invitation metadata (including the
 * invitee email) in responses.</p>
 */
public class InviteeNotFoundException extends RuntimeException {

    public InviteeNotFoundException(String email) {
        super("An existing account is required before this invitation can be accepted.");
        // The invited address is retained server-side only; it must never reach the HTTP response.
    }
}