package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaObservationEvidenceRepository — Spring Data repository over
 * {@link ObservationEvidenceJpaEntity}. Snapshots are identified by stable identity and
 * listed by domain or owning organization. No update or delete queries are defined.
 */
public interface JpaObservationEvidenceRepository extends JpaRepository<ObservationEvidenceJpaEntity, String> {

    List<ObservationEvidenceJpaEntity> findByDomainId(String domainId);

    List<ObservationEvidenceJpaEntity> findByOrganizationId(String organizationId);
}
