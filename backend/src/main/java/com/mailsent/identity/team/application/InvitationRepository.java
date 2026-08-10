package com.mailsent.identity.team.application;

import java.time.Instant;
import java.util.Optional;

import com.mailsent.identity.team.domain.Invitation;

/**
 * InvitationRepository — the app.owned port for persisting and retrieving invitations.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. Only the token hash
 * is ever stored (never the raw acceptance token). The port exposes the three methods
 * frozen by the v8 contract (§7): {@link #save}, {@link #findByTokenHash}, and
 * {@link #markAcceptedIfPending}, which performs the atomic conditional
 * {@code PENDING → ACCEPTED} transition and is the sole authoritative concurrency
 * mechanism for single-use.</p>
 */
public interface InvitationRepository {

    /** Persists an invitation and returns it. */
    Invitation save(Invitation invitation);

    /** Loads an invitation by its stored token hash, if present. */
    Optional<Invitation> findByTokenHash(String tokenHash);

    /**
     * Atomically transitions a still-PENDING invitation to ACCEPTED with the given
     * acceptance instant. Returns the number of affected rows: {@code 1} when the
     * conditional transition succeeded, {@code 0} when the invitation was no longer
     * PENDING at that instant. Executes inside the caller's transaction (never
     * {@code REQUIRES_NEW}); exactly one concurrent caller may observe {@code 1}.
     */
    int markAcceptedIfPending(String id, Instant acceptedAt);
}