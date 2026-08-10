package com.mailsent.identity.team.application;

import com.mailsent.identity.team.domain.Invitation;

/**
 * InvitationIssued — the result of creating an invitation.
 *
 * <p>Carries the persisted {@link Invitation} together with the raw acceptance token that
 * must be delivered to the invitee. The raw token is returned exactly once and is never
 * stored (TEAM-001 does not perform outbound email delivery; delivery is a separate
 * approved task).</p>
 *
 * @param invitation the persisted invitation
 * @param rawToken   the raw, never-stored acceptance token to be delivered to the invitee
 */
public record InvitationIssued(Invitation invitation, String rawToken) {
}