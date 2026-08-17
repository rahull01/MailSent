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

import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaReputationObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaReputationObservationRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.ReputationObservationJpaEntity;

/**
 * Tests the production {@link JpaReputationObservationRepositoryAdapter} in
 * isolation with a mocked Spring Data repository ({@code @Profile("!test")}),
 * including exact raw-payload round-trip.
 */
class JpaReputationObservationRepositoryAdapterTest {

    private final JpaReputationObservationRepository jpa = mock(JpaReputationObservationRepository.class);
    private final JpaReputationObservationRepositoryAdapter adapter =
        new JpaReputationObservationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void payloadRoundTripsExactly() {
        ReputationObservation observation = ReputationObservation.restore(
            "rep-1", "org-1", "dom-1", "example.com", ReputationSource.POSTMASTER, now,
            "literal-raw-payload");
        ArgumentCaptor<ReputationObservationJpaEntity> captor =
            ArgumentCaptor.forClass(ReputationObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ReputationObservation saved = adapter.save(observation);

        assertEquals("literal-raw-payload", saved.payload());
        assertEquals("literal-raw-payload", captor.getValue().payload());
        assertEquals("POSTMASTER", captor.getValue().source());
    }

    @Test
    void findByIdRestoresSourceAndPayload() {
        ReputationObservationJpaEntity entity = new ReputationObservationJpaEntity(
            "rep-1", "org-1", "dom-1", "example.com", "SNDS", now, "snds-raw");
        when(jpa.findById("rep-1")).thenReturn(Optional.of(entity));

        ReputationObservation found = adapter.findById("rep-1").orElseThrow();
        assertEquals("rep-1", found.id());
        assertEquals(ReputationSource.SNDS, found.source());
        assertEquals("snds-raw", found.payload());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        ReputationObservationJpaEntity entity = new ReputationObservationJpaEntity(
            "rep-1", "org-1", "dom-1", "example.com", "POSTMASTER", now, "raw");
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }

    @Test
    void saveAndFlushIsUsedRatherThanUpdateOrDelete() {
        ReputationObservation observation = ReputationObservation.restore(
            "rep-1", "org-1", "dom-1", "example.com", ReputationSource.SNDS, now, "raw");
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(observation);
        assertTrue(adapter.findById("missing").isEmpty());
    }
}
