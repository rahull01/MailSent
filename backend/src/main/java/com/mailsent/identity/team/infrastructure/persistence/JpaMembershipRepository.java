package com.mailsent.identity.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaMembershipRepository — Spring Data repository over {@link MembershipJpaEntity}. The
 * {@code uk_memberships_user_organization} unique constraint makes a (user, organization)
 * pair resolve to at most one membership; {@code existsByUserAndOrganizationId} supports
 * the duplicate-membership pre-check.
 */
public interface JpaMembershipRepository extends JpaRepository<MembershipJpaEntity, String> {

    boolean existsByUserIdAndOrganizationId(String userId, String organizationId);
}