package com.mailsent.identity.team.application;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;

/**
 * TeamQueryService — Application service for querying team members within an organization boundary (TEAM-004).
 *
 * <p>Enforces:
 * 1. Multi-Tenant Isolation (INV-09): The calling user can only query members of their own home organization.
 * 2. Role Visibility (FUN-D5): Authenticated OWNER, ANALYST, and VIEWER roles are permitted to list team members.
 * 3. Zero Secrets (N4): Maps User domain entities to safe {@link TeamMemberSummary} records without passwordHash.
 * 4. Deterministic Ordering: Preserves createdAt ASC, id ASC.</p>
 */
@Service
public class TeamQueryService {

    private final UserRepository userRepository;

    public TeamQueryService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
    }

    /**
     * Lists active team members of the target organization for an authorized caller.
     *
     * @param caller               the authenticated user making the query
     * @param targetOrganizationId the organization whose team is being queried
     * @return list of team member summaries ordered chronologically
     * @throws AccessDeniedException if caller is unauthenticated or belongs to a different organization (INV-09)
     */
    public List<TeamMemberSummary> listTeamMembers(User caller, String targetOrganizationId) {
        if (caller == null) {
            throw new AccessDeniedException("anonymous", targetOrganizationId, null, "Caller is unauthenticated.");
        }

        if (targetOrganizationId == null || targetOrganizationId.isBlank()) {
            throw new AccessDeniedException(caller.id(), "none", null, "Target organization ID must not be blank.");
        }

        // Multi-Tenant Isolation Invariant (INV-09)
        if (caller.organizationId() == null || !caller.organizationId().equals(targetOrganizationId)) {
            throw new AccessDeniedException(caller.id(), targetOrganizationId, null,
                    String.format("Multi-tenant violation: user home org '%s' does not match target org '%s'",
                            caller.organizationId(), targetOrganizationId));
        }

        return userRepository.findAllByOrganizationId(targetOrganizationId).stream()
                .map(u -> new TeamMemberSummary(u.id(), u.email().value(), u.role(), u.createdAt()))
                .toList();
    }
}
