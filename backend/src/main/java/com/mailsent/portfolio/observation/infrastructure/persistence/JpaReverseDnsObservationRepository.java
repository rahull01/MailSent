package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaReverseDnsObservationRepository — Spring Data repository over
 * {@link ReverseDnsObservationJpaEntity}. Snapshots are identified by stable
 * identity and listed by domain or owning organization. No update or delete
 * queries are defined.
 */
public interface JpaReverseDnsObservationRepository extends JpaRepository<ReverseDnsObservationJpaEntity, String> {

    List<ReverseDnsObservationJpaEntity> findByDomainId(String domainId);

    List<ReverseDnsObservationJpaEntity> findByOrganizationId(String organizationId);
}
