package com.mailsent.monitoring.application;

import java.util.List;

import com.mailsent.monitoring.domain.HealthChangeEvent;

public interface HealthChangeEventRepository {
    HealthChangeEvent save(HealthChangeEvent event);
    List<HealthChangeEvent> findByDomainId(String domainId);
}
