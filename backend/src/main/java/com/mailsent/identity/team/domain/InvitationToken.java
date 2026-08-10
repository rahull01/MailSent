package com.mailsent.identity.team.domain;

/**
 * InvitationToken — the credential authorizing acceptance of an {@link Invitation}.
 *
 * <p>An invitation has a transient <em>raw token</em> (returned to the issuer once, to be
 * delivered to the invitee) and a stored <em>token hash</em>. Only the hash is ever
 * persisted; possession of the raw token is the authority to accept. The raw value is
 * generated with high entropy by an {@link InvitationTokenFactory} and never logged or
 * stored (mirrors the Session token rule).</p>
 *
 * @param rawToken the value returned once to be delivered to the invitee
 * @param tokenHash the irreversible stored representation used to look up the invitation
 */
public record InvitationToken(String rawToken, String tokenHash) {
}