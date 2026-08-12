package com.mailsent.portfolio.client.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaClientRelationshipRepository — Spring Data repository over
 * {@link ClientRelationshipJpaEntity}. A relationship is identified by its stable identity;
 * the agency-scoped read supports the PORT-001 Portfolio container.
 */
public interface JpaClientRelationshipRepository extends JpaRepository<ClientRelationshipJpaEntity, String> {

    List<ClientRelationshipJpaEntity> findByAgencyOrganizationId(String agencyOrganizationId);
}