package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.infrastructure.persistence.HealthSignalJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaHealthSignalRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaHealthSignalRepositoryAdapter;

/**
 * Tests the production {@link JpaHealthSignalRepositoryAdapter} in isolation
 * with a mocked Spring Data repository ({@code @Profile("!test")}), including
 * lineage id-list JSON round-trip.
 */
class JpaHealthSignalRepositoryAdapterTest {

    private final JpaHealthSignalRepository jpa = mock(JpaHealthSignalRepository.class);
    private final JpaHealthSignalRepositoryAdapter adapter = new JpaHealthSignalRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-15T10:00:00Z");

    @Test
    void lineageIdListsRoundTripAsJsonArrays() {
        HealthSignal signal = HealthSignal.restore(
            "hs-1", "org-1", "dom-1", "example.com", HealthIndication.UNKNOWN, "oe-1",
            List.of("agg-1", "agg-2"), List.of("rep-1"), now);
        ArgumentCaptor<HealthSignalJpaEntity> captor = ArgumentCaptor.forClass(HealthSignalJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        HealthSignal saved = adapter.save(signal);

        assertEquals(List.of("agg-1", "agg-2"), saved.dmarcAggregateEvidenceIds());
        assertEquals("[\"agg-1\",\"agg-2\"]", captor.getValue().dmarcAggregateEvidenceIds());
        assertEquals("[\"rep-1\"]", captor.getValue().reputationObservationIds());
        assertEquals("unknown", captor.getValue().indication());
        assertEquals("oe-1", captor.getValue().observationEvidenceId());
    }

    @Test
    void findByIdRestoresIndicationAndLineage() {
        HealthSignalJpaEntity entity = new HealthSignalJpaEntity(
            "hs-1", "org-1", "dom-1", "example.com", "unknown", "oe-1",
            "[\"agg-1\"]", "[\"rep-1\"]", now);
        when(jpa.findById("hs-1")).thenReturn(Optional.of(entity));

        HealthSignal found = adapter.findById("hs-1").orElseThrow();
        assertEquals(HealthIndication.UNKNOWN, found.indication());
        assertEquals(List.of("agg-1"), found.dmarcAggregateEvidenceIds());
        assertEquals(List.of("rep-1"), found.reputationObservationIds());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        HealthSignalJpaEntity entity = new HealthSignalJpaEntity(
            "hs-1", "org-1", "dom-1", "example.com", "unknown", "oe-1",
            "[\"agg-1\"]", "[\"rep-1\"]", now);
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }

    @Test
    void saveAndFlushIsUsedRatherThanUpdateOrDelete() {
        HealthSignal signal = HealthSignal.restore(
            "hs-1", "org-1", "dom-1", "example.com", HealthIndication.UNKNOWN, "oe-1",
            List.of("agg-1"), List.of("rep-1"), now);
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(signal);
        assertTrue(adapter.findById("missing").isEmpty());
    }
}
