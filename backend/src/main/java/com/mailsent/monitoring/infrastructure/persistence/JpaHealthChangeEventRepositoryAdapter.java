package com.mailsent.monitoring.infrastructure.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.mailsent.monitoring.application.HealthChangeEventRepository;
import com.mailsent.monitoring.domain.HealthChangeEvent;

@Repository
@Profile("!test")
public class JpaHealthChangeEventRepositoryAdapter implements HealthChangeEventRepository {

    private final SpringDataHealthChangeEventRepository repo;

    public JpaHealthChangeEventRepositoryAdapter(SpringDataHealthChangeEventRepository repo) {
        this.repo = repo;
    }

    @Override
    public HealthChangeEvent save(HealthChangeEvent event) {
        HealthChangeEventEntity entity = new HealthChangeEventEntity();
        entity.setId(event.id());
        entity.setOrganizationId(event.organizationId());
        entity.setDomainId(event.domainId());
        entity.setPreviousIndication(event.previousIndication());
        entity.setCurrentIndication(event.currentIndication());
        entity.setDetectedAt(event.detectedAt());
        repo.save(entity);
        return event;
    }

    @Override
    public List<HealthChangeEvent> findByOrganizationIdAndDomainId(String organizationId, String domainId) {
        return repo.findByOrganizationIdAndDomainIdOrderByDetectedAtDesc(organizationId, domainId).stream()
            .map(e -> new HealthChangeEvent(
                e.getId(),
                e.getOrganizationId(),
                e.getDomainId(),
                e.getPreviousIndication(),
                e.getCurrentIndication(),
                e.getDetectedAt()
            ))
            .collect(Collectors.toList());
    }
}

