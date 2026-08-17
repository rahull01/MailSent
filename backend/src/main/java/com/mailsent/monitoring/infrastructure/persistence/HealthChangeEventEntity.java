package com.mailsent.monitoring.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.observation.domain.HealthIndication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_change_events")
public class HealthChangeEventEntity {
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
