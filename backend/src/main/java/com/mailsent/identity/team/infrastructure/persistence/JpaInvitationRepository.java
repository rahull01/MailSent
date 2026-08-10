package com.mailsent.identity.team.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaInvitationRepository — Spring Data repository over {@link InvitationJpaEntity}.
 * Lookup is by the stored token hash for the single-use acceptance flow; the
 * {@code uk_invitations_token_hash} unique index makes each hash resolve to at most one
 * invitation.
 */
public interface JpaInvitationRepository extends JpaRepository<InvitationJpaEntity, String> {

    Optional<InvitationJpaEntity> findByTokenHash(String tokenHash);
}