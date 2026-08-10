package com.mailsent.identity.team.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.domain.PendingInvitationAlreadyExistsException;

/**
 * JpaInvitationRepositoryAdapter — production {@link InvitationRepository} backed by
 * PostgreSQL. Active in the non-test profile; the hermetic in-memory adapter backs tests.
 *
 * <p><strong>Creation</strong> maps the PENDING-scoped partial unique index violation to
 * {@link PendingInvitationAlreadyExistsException} (duplicate-pending invariant, v8 §5).
 * <strong>Acceptance</strong> single-use is enforced by {@link #markAcceptedIfPending},
 * which runs the atomic conditional UPDATE inside the caller's transaction (never
 * {@code REQUIRES_NEW}) and reports the affected-row count — the sole authoritative
 * concurrency mechanism (v8 §7/§8).</p>
 */
@Repository
@Profile("!test")
public class JpaInvitationRepositoryAdapter implements InvitationRepository {

    private final JpaInvitationRepository jpa;

    public JpaInvitationRepositoryAdapter(JpaInvitationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Invitation save(Invitation invitation) {
        try {
            return toDomain(jpa.saveAndFlush(toEntity(invitation)));
        } catch (DataIntegrityViolationException ex) {
            // The PENDING-scoped partial unique index rejected a duplicate (org, email) pending
            // invitation; surface the defined domain-level result (v8 §5).
            throw new PendingInvitationAlreadyExistsException(invitation.organizationId(),
                invitation.inviteeEmail().value());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public int markAcceptedIfPending(String id, Instant acceptedAt) {
        // Runs inside the caller's acceptance transaction (REQUIRED propagation); never
        // REQUIRES_NEW. The atomic conditional UPDATE decides concurrent single-use.
        return jpa.markAcceptedIfPending(id, acceptedAt);
    }

    private InvitationJpaEntity toEntity(Invitation invitation) {
        return new InvitationJpaEntity(invitation.id(), invitation.organizationId(),
            invitation.inviteeEmail().value(), invitation.role(), invitation.tokenHash(),
            invitation.status(), invitation.createdAt(), invitation.acceptedAt());
    }

    private Invitation toDomain(InvitationJpaEntity entity) {
        return Invitation.restore(entity.id(), entity.organizationId(), new Email(entity.inviteeEmail()),
            entity.role(), entity.tokenHash(), entity.status(), entity.createdAt(), entity.acceptedAt());
    }
}