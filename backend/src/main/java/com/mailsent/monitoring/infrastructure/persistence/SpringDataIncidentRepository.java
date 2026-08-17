package com.mailsent.monitoring.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, String> {
    Optional<IncidentEntity> findByDomainIdAndResolvedAtIsNull(String domainId);
}
