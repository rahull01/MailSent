package com.mailsent.monitoring.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidents")
public class IncidentEntity {
    @Id
    private String id;
    @Column(name = "domain_id", nullable = false)
    private String domainId;
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
    
    public Instant getOpenedAt() { return openedAt; }
    public void setOpenedAt(Instant openedAt) { this.openedAt = openedAt; }
    
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
