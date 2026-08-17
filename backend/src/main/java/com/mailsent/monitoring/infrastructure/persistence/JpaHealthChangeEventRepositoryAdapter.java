package com.mailsent.monitoring.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mailsent.monitoring.application.HealthChangeEventRepository;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Repository
public class JpaHealthChangeEventRepositoryAdapter implements HealthChangeEventRepository {

    private final SpringDataHealthChangeEventRepository repo;

    public JpaHealthChangeEventRepositoryAdapter(SpringDataHealthChangeEventRepository repo) {
        this.repo = repo;
    }

    @Override
    public HealthChangeEvent save(HealthChangeEvent event) {
        HealthChangeEventEntity entity = new HealthChangeEventEntity();
        entity.setId(event.id());
        entity.setDomainId(event.domainId());
        entity.setPreviousIndication(event.previousIndication());
        entity.setCurrentIndication(event.currentIndication());
        entity.setDetectedAt(event.detectedAt());
        repo.save(entity);
        return event;
    }

    @Override
    public List<HealthChangeEvent> findByDomainId(String domainId) {
        return repo.findByDomainIdOrderByDetectedAtDesc(domainId).stream()
            .map(e -> new HealthChangeEvent(
                e.getId(),
                e.getDomainId(),
                e.getPreviousIndication(),
                e.getCurrentIndication(),
                e.getDetectedAt()
            ))
            .collect(Collectors.toList());
    }
}

interface SpringDataHealthChangeEventRepository extends JpaRepository<HealthChangeEventEntity, String> {
    List<HealthChangeEventEntity> findByDomainIdOrderByDetectedAtDesc(String domainId);
}

@Entity
@Table(name = "health_change_events")
class HealthChangeEventEntity {
    @Id
    private String id;
    @Column(name = "domain_id", nullable = false)
    private String domainId;
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_indication")
    private HealthIndication previousIndication;
    @Enumerated(EnumType.STRING)
    @Column(name = "current_indication", nullable = false)
    private HealthIndication currentIndication;
    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
    
    public HealthIndication getPreviousIndication() { return previousIndication; }
    public void setPreviousIndication(HealthIndication previousIndication) { this.previousIndication = previousIndication; }
    
    public HealthIndication getCurrentIndication() { return currentIndication; }
    public void setCurrentIndication(HealthIndication currentIndication) { this.currentIndication = currentIndication; }
    
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
