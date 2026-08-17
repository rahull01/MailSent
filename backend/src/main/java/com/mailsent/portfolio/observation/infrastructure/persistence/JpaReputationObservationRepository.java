package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaReputationObservationRepository — Spring Data repository over
 * {@link ReputationObservationJpaEntity}. Snapshots are identified by stable
 * identity and listed by domain or owning organization. No update or delete
 * queries are defined.
 */
public interface JpaReputationObservationRepository extends JpaRepository<ReputationObservationJpaEntity, String> {

    List<ReputationObservationJpaEntity> findByDomainId(String domainId);

    List<ReputationObservationJpaEntity> findByOrganizationId(String organizationId);
}
