package com.mailsent.identity.team.interfaces;

/**
 * AcceptInvitationResponse — the outbound acceptance contract for TEAM-001. Confirms the
 * membership established. The user's home Organization is intentionally not exposed here
 * (it is unchanged by acceptance and stays internal to the backend).
 *
 * @param membershipId    the stable identity of the created membership
 * @param userId          the stable identity of the member
 * @param organizationId  the Organization joined
 * @param role            the granted role (business label)
 */
public record AcceptInvitationResponse(String membershipId, String userId, String organizationId, String role) {
}