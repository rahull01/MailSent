package com.mailsent.identity.team.infrastructure;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.domain.Invitation;

/**
 * InMemoryInvitationRepository — a hermetic {@link InvitationRepository} for the test
 * profile. Invitations are keyed by their stable identity, with a token-hash index for the
 * acceptance lookup. {@link #markAcceptedIfPending} applies the same rule as the database
 * conditional transition on the shared aggregate instance and reports the affected row count.
 */
public class InMemoryInvitationRepository implements InvitationRepository {

    private final Map<String, Invitation> byId = new ConcurrentHashMap<>();
    private final Map<String, Invitation> byTokenHash = new ConcurrentHashMap<>();

    @Override
    public Invitation save(Invitation invitation) {
        byId.put(invitation.id(), invitation);
        byTokenHash.put(invitation.tokenHash(), invitation);
        return invitation;
    }

    @Override
    public Optional<Invitation> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(byTokenHash.get(tokenHash));
    }

    @Override
    public int markAcceptedIfPending(String id, Instant acceptedAt) {
        Invitation invitation = byId.get(id);
        if (invitation == null) {
            return 0;
        }
        // Guard against a concurrent accept of the same in-memory invitation.
        synchronized (invitation) {
            return invitation.accept(acceptedAt) ? 1 : 0;
        }
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
        byTokenHash.clear();
    }
}