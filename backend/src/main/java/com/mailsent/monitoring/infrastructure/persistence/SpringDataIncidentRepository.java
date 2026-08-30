package com.mailsent.monitoring.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, String> {
    Optional<IncidentEntity> findByOrganizationIdAndDomainIdAndResolvedAtIsNull(String organizationId, String domainId);
    Optional<IncidentEntity> findByOrganizationIdAndId(String organizationId, String id);
}

