package com.mailsent.explanation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.explanation.domain.Explanation;
import com.mailsent.explanation.domain.ExplanationMode;
import com.mailsent.explanation.domain.ExplanationView;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class ExplanationServiceTest {

    @Test
    void explainsLatestHealthSignal() {
        HealthSignalService healthSignalService = mock(HealthSignalService.class);
        ExplanationService service = new ExplanationService(healthSignalService);

        HealthSignal older = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now().minusSeconds(100));
        HealthSignal newer = HealthSignal.restore("hs-2", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-2", List.of("agg-2"), List.of("rep-2"), Instant.now());

        when(healthSignalService.findByDomainId("dom-1")).thenReturn(List.of(older, newer));

        Optional<Explanation> result = service.explainLatest("dom-1");

        assertTrue(result.isPresent());
        assertEquals("hs-2", result.get().grounding().sourceSignalId());
        assertTrue(result.get().whatHappened().contains("at risk"));
    }

    @Test
    void explainsLatestWithModeAndEnforcesTenantIsolation() {
        HealthSignalService healthSignalService = mock(HealthSignalService.class);
        DomainRepository domainRepository = mock(DomainRepository.class);
        ExplanationService service = new ExplanationService(healthSignalService, domainRepository);

        Domain domain = Domain.restore("dom-1", "org-1", "example.com", Instant.now().minusSeconds(86400));
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
                HealthIndication.AT_RISK, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
        when(healthSignalService.findByDomainId("dom-1")).thenReturn(List.of(signal));

        Optional<ExplanationView> view = service.explainLatestWithMode("org-1", "dom-1", ExplanationMode.BUSINESS_SUMMARY);

        assertThat(view).isPresent();
        assertThat(view.get().mode()).isEqualTo(ExplanationMode.BUSINESS_SUMMARY);
        assertThat(view.get().grounding().sourceSignalId()).isEqualTo("hs-1");

        // Tenant mismatch rejection
        assertThatThrownBy(() -> service.explainLatestWithMode("org-other", "dom-1", ExplanationMode.BUSINESS_SUMMARY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant boundary violation");
    }

    @Test
    void returnsEmptyWhenNoSignalExists() {
        HealthSignalService healthSignalService = mock(HealthSignalService.class);
        ExplanationService service = new ExplanationService(healthSignalService);

        when(healthSignalService.findByDomainId("dom-1")).thenReturn(List.of());

        assertTrue(service.explainLatest("dom-1").isEmpty());
    }
}
