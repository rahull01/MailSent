package com.mailsent.identity.team.interfaces;

/**
 * AcceptInvitationResponse — the exact TEAM-001 acceptance success contract (v8 §11).
 *
 * <p>The body is exactly {@code {"accepted":true,"invitationId":"<id>"}}. No token, email,
 * organization data, role, tokenHash, or other invitation metadata is returned.</p>
 *
 * @param accepted      always {@code true} on a successful acceptance
 * @param invitationId  the stable identity of the accepted invitation
 */
public record AcceptInvitationResponse(boolean accepted, String invitationId) {
}