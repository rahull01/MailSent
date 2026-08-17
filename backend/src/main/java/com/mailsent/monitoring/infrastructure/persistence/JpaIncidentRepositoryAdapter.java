package com.mailsent.monitoring.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mailsent.monitoring.application.IncidentRepository;
import com.mailsent.monitoring.domain.Incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Repository
public class JpaIncidentRepositoryAdapter implements IncidentRepository {

    private final SpringDataIncidentRepository repo;

    public JpaIncidentRepositoryAdapter(SpringDataIncidentRepository repo) {
        this.repo = repo;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = new IncidentEntity();
        entity.setId(incident.id());
        entity.setDomainId(incident.domainId());
        entity.setOpenedAt(incident.openedAt());
        entity.setResolvedAt(incident.resolvedAt());
        repo.save(entity);
        return incident;
    }

    @Override
    public Optional<Incident> findActiveByDomainId(String domainId) {
        return repo.findByDomainIdAndResolvedAtIsNull(domainId)
            .map(e -> new Incident(e.getId(), e.getDomainId(), e.getOpenedAt(), e.getResolvedAt()));
    }

    @Override
    public Optional<Incident> findById(String id) {
        return repo.findById(id)
            .map(e -> new Incident(e.getId(), e.getDomainId(), e.getOpenedAt(), e.getResolvedAt()));
    }
}
