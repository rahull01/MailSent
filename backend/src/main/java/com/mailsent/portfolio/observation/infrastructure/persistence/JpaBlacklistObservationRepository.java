package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaBlacklistObservationRepository — Spring Data repository over
 * {@link BlacklistObservationJpaEntity}. Snapshots are identified by stable
 * identity and listed by domain or owning organization. No update or delete
 * queries are defined.
 */
public interface JpaBlacklistObservationRepository extends JpaRepository<BlacklistObservationJpaEntity, String> {

    List<BlacklistObservationJpaEntity> findByDomainId(String domainId);

    List<BlacklistObservationJpaEntity> findByOrganizationId(String organizationId);
}
