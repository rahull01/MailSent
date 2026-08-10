package com.mailsent.identity.team.domain;

/**
 * InvitationTokenFactory — the port for producing a hardened invitation token.
 *
 * <p>Placed in the domain so the security rule ("an invitation token is high-entropy and is
 * stored only as a hash") is a business contract, mirroring {@code SessionTokenFactory}. An
 * infrastructure adapter satisfies it with {@code SecureRandom} for the raw value and a
 * one-way hash (SHA-256) for the stored representation. {@link #hash} lets the service
 * re-derive the stored hash from a presented raw token for single-use lookup.</p>
 */
public interface InvitationTokenFactory {

    /** Creates a fresh, unpredictable invitation token with its stored hash representation. */
    InvitationToken randomToken();

    /** The irreversible stored hash of a presented raw token (for lookup on acceptance). */
    String hash(String rawToken);
}