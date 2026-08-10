package com.mailsent.identity.team.application;

/**
 * InvitationCreationResult — the internal result of creating an invitation (frozen v8 §5).
 *
 * <p>Carries the persisted {@code invitationId} together with the raw acceptance token that
 * must be delivered to the invitee. The raw token exists only in this in-memory creation
 * result, is returned exactly once to the internal caller, is never part of the persisted
 * Invitation entity, is never returned by the repository, cannot be retrieved later from the
 * database, and is never logged, traced, audited, emailed, or exposed through HTTP. No
 * token-retrieval endpoint is created.</p>
 *
 * @param invitationId the stable identity of the persisted PENDING invitation
 * @param rawToken     the raw, never-stored acceptance token to be delivered to the invitee
 */
public record InvitationCreationResult(String invitationId, String rawToken) {
}