package com.mailsent.identity.team.infrastructure.persistence;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.domain.Invitation;

/**
 * JpaInvitationRepositoryAdapter — production {@link InvitationRepository} backed by
 * PostgreSQL. Active in the non-test profile; the hermetic in-memory adapter backs tests.
 * {@link Transactional} keeps invitation creation and acceptance atomic.
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
        return toDomain(jpa.saveAndFlush(toEntity(invitation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private InvitationJpaEntity toEntity(Invitation invitation) {
        return new InvitationJpaEntity(invitation.id(), invitation.organizationId(),
            invitation.inviteeEmail().value(), invitation.role(), invitation.tokenHash(),
            invitation.status(), invitation.createdAt());
    }

    private Invitation toDomain(InvitationJpaEntity entity) {
        return Invitation.restore(entity.id(), entity.organizationId(), new Email(entity.inviteeEmail()),
            entity.role(), entity.tokenHash(), entity.status(), entity.createdAt());
    }
}