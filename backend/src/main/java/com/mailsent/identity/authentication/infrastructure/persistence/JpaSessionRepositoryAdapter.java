package com.mailsent.identity.authentication.infrastructure.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.identity.authentication.application.SessionRepository;
import com.mailsent.identity.authentication.domain.Session;

/**
 * JpaSessionRepositoryAdapter — production {@link SessionRepository} backed by
 * PostgreSQL. Active in every non-test profile; the hermetic in-memory adapter and this
 * bean are never both active together.
 *
 * <p>The {@link Transactional} writer makes session creation atomic: a session is either
 * fully recorded (with its token hash and expiry) or not at all.</p>
 */
@Repository
@Profile("!test")
public class JpaSessionRepositoryAdapter implements SessionRepository {

    private final JpaSessionRepository jpa;

    public JpaSessionRepositoryAdapter(JpaSessionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Session save(Session session) {
        SessionJpaEntity entity = new SessionJpaEntity(
            session.id(), session.userId(), session.tokenHash(), session.createdAt(), session.expiresAt());
        SessionJpaEntity saved = jpa.saveAndFlush(entity);
        return toSession(saved);
    }

    private Session toSession(SessionJpaEntity entity) {
        return Session.restore(entity.id(), entity.userId(), entity.tokenHash(), entity.createdAt(), entity.expiresAt());
    }
}