package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaDmarcAggregateEvidenceRepository — Spring Data repository over
 * {@link DmarcAggregateEvidenceJpaEntity}. Snapshots are identified by stable
 * identity and listed by domain, organization, or domain+report_id. No update
 * or delete queries are defined.
 */
public interface JpaDmarcAggregateEvidenceRepository extends JpaRepository<DmarcAggregateEvidenceJpaEntity, String> {

    List<DmarcAggregateEvidenceJpaEntity> findByDomainId(String domainId);

    List<DmarcAggregateEvidenceJpaEntity> findByOrganizationId(String organizationId);

    List<DmarcAggregateEvidenceJpaEntity> findByDomainIdAndReportId(String domainId, String reportId);
}
