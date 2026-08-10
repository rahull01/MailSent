package com.mailsent.portfolio.client.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaClientRelationshipRepository — Spring Data repository over
 * {@link ClientRelationshipJpaEntity}. A relationship is identified by its stable identity;
 * only the lifecycle read declared by CLIENT-001 exists.
 */
public interface JpaClientRelationshipRepository extends JpaRepository<ClientRelationshipJpaEntity, String> {
}