package com.mailsent.identity.team.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.identity.team.application.MembershipRepository;
import com.mailsent.identity.team.domain.Membership;

/**
 * InMemoryMembershipRepository — a hermetic, thread-safe {@link MembershipRepository} for
 * the test profile. Memberships are keyed by their stable identity, with a composite
 * (user, organization) index for the duplicate-membership check.
 */
public class InMemoryMembershipRepository implements MembershipRepository {

    private final Map<String, Membership> byId = new ConcurrentHashMap<>();
    private final Map<String, Membership> byUserOrganization = new ConcurrentHashMap<>();

    @Override
    public Membership save(Membership membership) {
        byId.put(membership.id(), membership);
        byUserOrganization.put(key(membership.userId(), membership.organizationId()), membership);
        return membership;
    }

    @Override
    public boolean existsByUserAndOrganization(String userId, String organizationId) {
        return byUserOrganization.containsKey(key(userId, organizationId));
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
        byUserOrganization.clear();
    }

    private String key(String userId, String organizationId) {
        return userId + "::" + organizationId;
    }
}