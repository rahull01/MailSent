package com.mailsent.identity.authentication.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaSessionRepository — the Spring Data repository over {@link SessionJpaEntity}.
 */
public interface JpaSessionRepository extends JpaRepository<SessionJpaEntity, String> {

    Optional<SessionJpaEntity> findByTokenHash(String tokenHash);
}