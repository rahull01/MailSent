package com.mailsent.identity.team.infrastructure;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.domain.Invitation;

/**
 * InMemoryInvitationRepository — a hermetic, thread-safe {@link InvitationRepository} for
 * the test profile. Invitations are keyed by their stable identity, with a secondary
 * token-hash index for the single-use acceptance lookup.
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

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
        byTokenHash.clear();
    }
}