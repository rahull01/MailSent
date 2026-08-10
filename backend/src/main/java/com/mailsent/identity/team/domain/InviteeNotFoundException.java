package com.mailsent.identity.team.domain;

/**
 * Raised when an invitation is accepted but the invited email has no existing account.
 *
 * <p>TEAM-001 deliberately does not invent account creation on acceptance (decision 9);
 * the new-user path (accepting an invitation requires signup) is a separate, explicitly
 * approved decision. Until then, the acceptance operation refuses with this clear domain
 * error rather than silently redesigning the signup flow.</p>
 */
public class InviteeNotFoundException extends RuntimeException {

    public InviteeNotFoundException(String email) {
        super("No account exists for '" + email + "'; it must be created before the invitation can be accepted.");
    }
}