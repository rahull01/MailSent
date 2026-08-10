package com.mailsent.identity.team.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JpaInvitationRepository — Spring Data repository over {@link InvitationJpaEntity}.
 *
 * <p>Lookup is by the stored token hash for the acceptance flow; the
 * {@code uk_invitations_token_hash} UNIQUE index makes each hash identify at most one
 * invitation. {@link #markAcceptedIfPending} is the FROZEN atomic conditional transition
 * (v8 §7/§8): it updates exactly the row that is still {@code PENDING} and returns the
 * affected-row count, so exactly one concurrent caller observes {@code 1}. It executes in
 * the caller's transaction (never {@code REQUIRES_NEW}).</p>
 */
public interface JpaInvitationRepository extends JpaRepository<InvitationJpaEntity, String> {

    Optional<InvitationJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
        UPDATE InvitationJpaEntity i
           SET i.status = com.mailsent.identity.team.domain.Invitation.Status.ACCEPTED,
               i.acceptedAt = :acceptedAt
         WHERE i.id = :id
           AND i.status = com.mailsent.identity.team.domain.Invitation.Status.PENDING
        """)
    int markAcceptedIfPending(@Param("id") String id, @Param("acceptedAt") Instant acceptedAt);
}