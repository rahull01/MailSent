package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaHealthSignalRepository — Spring Data repository over
 * {@link HealthSignalJpaEntity}. Snapshots are identified by stable identity
 * and listed by domain or owning organization. No update or delete queries
 * are defined.
 */
public interface JpaHealthSignalRepository extends JpaRepository<HealthSignalJpaEntity, String> {

    List<HealthSignalJpaEntity> findByDomainId(String domainId);

    List<HealthSignalJpaEntity> findByOrganizationId(String organizationId);
}
