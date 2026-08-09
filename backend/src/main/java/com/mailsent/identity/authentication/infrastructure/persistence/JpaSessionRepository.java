package com.mailsent.identity.authentication.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaSessionRepository — the Spring Data repository over {@link SessionJpaEntity}.
 * AUTH-001 only creates sessions; retrieval/session-consumption queries belong to
 * AUTH-006 and are not declared here (scope discipline).</p>
 */
public interface JpaSessionRepository extends JpaRepository<SessionJpaEntity, String> {
}