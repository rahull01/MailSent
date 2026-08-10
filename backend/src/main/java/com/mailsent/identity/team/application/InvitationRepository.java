package com.mailsent.identity.team.application;

import java.util.Optional;

import com.mailsent.identity.team.domain.Invitation;

/**
 * InvitationRepository — the port for persisting and retrieving invitations.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. Only the token hash
 * is ever stored (never the raw acceptance token). {@link #findByTokenHash} supports the
 * single-use acceptance lookup.</p>
 */
public interface InvitationRepository {

    /** Persists an invitation and returns it. */
    Invitation save(Invitation invitation);

    /** Loads an invitation by its stored token hash, if present. */
    Optional<Invitation> findByTokenHash(String tokenHash);
}