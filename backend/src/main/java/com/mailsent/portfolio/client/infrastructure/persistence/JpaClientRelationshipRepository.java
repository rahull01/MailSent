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

    /**
     * Active lookup for CLIENT-002 idempotency. Duplicate ACTIVE rows are contract-legal
     * (V7 has no uniqueness constraint), so this MUST NOT be an {@code Optional}-returning
     * derived query — that throws {@code IncorrectResultSizeDataAccessException} the moment
     * two ACTIVE rows exist. Deterministic first-by-formation keeps the read safe.
     */
    Optional<ClientRelationshipJpaEntity> findFirstByAgencyOrganizationIdAndClientOrganizationIdAndStatusOrderByFormedAtAscIdAsc(
            String agencyOrganizationId, String clientOrganizationId, ClientRelationship.Status status);
}