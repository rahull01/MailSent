package com.mailsent.portfolio.observation.application;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.HealthTrend;

/**
 * HealthTrendService — application service for deriving Health Trend projections (HEALTH-002).
 */
@Service
public class HealthTrendService {

    private final HealthSignalRepository healthSignalRepository;
    private final DomainRepository domainRepository;
    private final Clock clock;

    public HealthTrendService(HealthSignalRepository healthSignalRepository,
                              DomainRepository domainRepository,
                              Clock clock) {
        this.healthSignalRepository = Objects.requireNonNull(healthSignalRepository, "healthSignalRepository cannot be null");
        this.domainRepository = Objects.requireNonNull(domainRepository, "domainRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Transactional(readOnly = true)
    public HealthTrend getTrendForDomain(String organizationId, String domainId, Instant since) {
        Objects.requireNonNull(organizationId, "organizationId cannot be null");
        Objects.requireNonNull(domainId, "domainId cannot be null");

        // 1. Verify registered domain and multi-tenant boundary (INV-09)
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new DomainNotRegisteredException("Domain not registered: " + domainId));

        if (!domain.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("Tenant boundary violation: organizationId does not own domain");
        }

        Instant now = clock.instant();
        Instant cutoff = since != null ? since : now.minus(30, ChronoUnit.DAYS);

        // 2. Fetch historical snapshots and filter by cutoff
        List<HealthSignal> historical = healthSignalRepository.findByDomainId(domainId).stream()
                .filter(signal -> signal.observedAt() != null && !signal.observedAt().isBefore(cutoff))
                .toList();

        // 3. Derive deterministic HealthTrend
        return HealthTrend.calculate(organizationId, domainId, domain.name(), historical, now);
    }
}
