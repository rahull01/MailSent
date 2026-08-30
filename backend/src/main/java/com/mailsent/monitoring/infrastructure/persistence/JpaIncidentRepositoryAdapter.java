package com.mailsent.monitoring.infrastructure.persistence;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.mailsent.monitoring.application.IncidentRepository;
import com.mailsent.monitoring.domain.Incident;

@Repository
@Profile("!test")
public class JpaIncidentRepositoryAdapter implements IncidentRepository {

    private final SpringDataIncidentRepository repo;

    public JpaIncidentRepositoryAdapter(SpringDataIncidentRepository repo) {
        this.repo = repo;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = new IncidentEntity();
        entity.setId(incident.id());
        entity.setOrganizationId(incident.organizationId());
        entity.setDomainId(incident.domainId());
        entity.setOpenedAt(incident.openedAt());
        entity.setResolvedAt(incident.resolvedAt());
        repo.save(entity);
        return incident;
    }

    @Override
    public Optional<Incident> findActiveByOrganizationIdAndDomainId(String organizationId, String domainId) {
        return repo.findByOrganizationIdAndDomainIdAndResolvedAtIsNull(organizationId, domainId)
            .map(e -> new Incident(e.getId(), e.getOrganizationId(), e.getDomainId(), e.getOpenedAt(), e.getResolvedAt()));
    }

    @Override
    public Optional<Incident> findById(String organizationId, String id) {
        return repo.findByOrganizationIdAndId(organizationId, id)
            .map(e -> new Incident(e.getId(), e.getOrganizationId(), e.getDomainId(), e.getOpenedAt(), e.getResolvedAt()));
    }
}

