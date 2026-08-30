package com.mailsent.monitoring.application;

import java.util.Optional;
import com.mailsent.monitoring.domain.Incident;

public interface IncidentRepository {
    Incident save(Incident incident);
    Optional<Incident> findActiveByOrganizationIdAndDomainId(String organizationId, String domainId);
    Optional<Incident> findById(String organizationId, String id);
}

