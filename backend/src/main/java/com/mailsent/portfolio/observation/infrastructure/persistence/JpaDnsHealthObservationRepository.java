package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaDnsHealthObservationRepository — Spring Data repository over
 * {@link DnsHealthObservationJpaEntity}. Snapshots are identified by stable
 * identity and listed by domain or owning organization. No update or delete
 * queries are defined.
 */
public interface JpaDnsHealthObservationRepository extends JpaRepository<DnsHealthObservationJpaEntity, String> {

    List<DnsHealthObservationJpaEntity> findByDomainId(String domainId);

    List<DnsHealthObservationJpaEntity> findByOrganizationId(String organizationId);
}
