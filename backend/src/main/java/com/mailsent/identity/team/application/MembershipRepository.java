package com.mailsent.identity.team.application;

import com.mailsent.identity.team.domain.Membership;

/**
 * MembershipRepository — the port for persisting organization memberships.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The durable adapter
 * relies on the {@code uk_memberships_user_organization} constraint as the race-safe
 * backstop against duplicate active membership for a user within an organization.</p>
 */
public interface MembershipRepository {

    /** Persists a membership and returns it. */
    Membership save(Membership membership);

    /** Whether a membership already exists for the given user in the given organization. */
    boolean existsByUserAndOrganization(String userId, String organizationId);
}