package com.mailsent.monitoring.application;

import java.util.Optional;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.monitoring.domain.Incident;
import com.mailsent.portfolio.observation.domain.HealthIndication;

public class IncidentService {
    
    private final IncidentRepository incidents;

    public IncidentService(IncidentRepository incidents) {
        this.incidents = incidents;
    }

    public void processHealthChange(HealthChangeEvent event) {
        if (event.currentIndication() == HealthIndication.AT_RISK) {
            Optional<Incident> active = incidents.findActiveByDomainId(event.domainId());
            if (active.isEmpty()) {
                Incident incident = Incident.open(event.domainId(), event.detectedAt());
                incidents.save(incident);
            }
        } else if (event.currentIndication() == HealthIndication.HEALTHY) {
            Optional<Incident> active = incidents.findActiveByDomainId(event.domainId());
            if (active.isPresent()) {
                Incident incident = active.get();
                incident.resolve(event.detectedAt());
                incidents.save(incident);
            }
        }
    }
}
