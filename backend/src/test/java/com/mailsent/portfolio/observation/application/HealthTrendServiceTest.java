package com.mailsent.portfolio.observation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.HealthTrend;
import com.mailsent.portfolio.observation.domain.TrendDirection;

@ExtendWith(MockitoExtension.class)
class HealthTrendServiceTest {

    @Mock
    private HealthSignalRepository healthSignalRepository;

    @Mock
    private DomainRepository domainRepository;

    private Clock clock;
    private HealthTrendService service;

    private static final Instant NOW = Instant.parse("2026-08-19T12:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new HealthTrendService(healthSignalRepository, domainRepository, clock);
    }

    private Domain buildDomain(String orgId, String domId, String name) {
        return Domain.restore(domId, orgId, name, NOW.minusSeconds(86400));
    }

    private HealthSignal signal(String id, String orgId, String domId, HealthIndication indication, Instant observedAt) {
        return HealthSignal.restore(id, orgId, domId, "example.com",
                indication, "oe-1", List.of("ev-1"), List.of("rep-1"), observedAt);
    }

    @Test
    void shouldGetTrendForDomainSuccessfully() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        List<HealthSignal> signals = List.of(
                signal("hs-1", "org-1", "dom-1", HealthIndication.AT_RISK, NOW.minusSeconds(3600)),
                signal("hs-2", "org-1", "dom-1", HealthIndication.HEALTHY, NOW.minusSeconds(600))
        );
        when(healthSignalRepository.findByDomainId("dom-1")).thenReturn(signals);

        HealthTrend trend = service.getTrendForDomain("org-1", "dom-1", null);

        assertThat(trend.direction()).isEqualTo(TrendDirection.IMPROVING);
        assertThat(trend.currentIndication()).isEqualTo(HealthIndication.HEALTHY);
        assertThat(trend.previousIndication()).isEqualTo(HealthIndication.AT_RISK);
        assertThat(trend.evaluatedHealthSignalIds()).containsExactly("hs-1", "hs-2");
    }

    @Test
    void shouldEnforceMultiTenantIsolationWhenOrgMismatch() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        // Caller claims org-other for domain owned by org-1
        assertThatThrownBy(() -> service.getTrendForDomain("org-other", "dom-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant boundary violation");
    }

    @Test
    void shouldThrowWhenDomainNotRegistered() {
        when(domainRepository.findById("dom-unregistered")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTrendForDomain("org-1", "dom-unregistered", null))
                .isInstanceOf(DomainNotRegisteredException.class);
    }
}
