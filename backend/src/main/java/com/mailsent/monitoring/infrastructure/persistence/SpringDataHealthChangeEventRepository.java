package com.mailsent.monitoring.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataHealthChangeEventRepository extends JpaRepository<HealthChangeEventEntity, String> {
    List<HealthChangeEventEntity> findByDomainIdOrderByDetectedAtDesc(String domainId);
}
