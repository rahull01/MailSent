package com.mailsent.portfolio.client.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * JpaClientRelationshipRepository — Spring Data repository over
 * {@link ClientRelationshipJpaEntity}. A relationship is identified by its stable identity;
 * the agency-scoped read supports the PORT-001 Portfolio container.
 */
public interface JpaClientRelationshipRepository extends JpaRepository<ClientRelationshipJpaEntity, String> {

    List<ClientRelationshipJpaEntity> findByAgencyOrganizationId(String agencyOrganizationId);

    Optional<ClientRelationshipJpaEntity> findByAgencyOrganizationIdAndClientOrganizationIdAndStatus(
            String agencyOrganizationId, String clientOrganizationId, ClientRelationship.Status status);
}