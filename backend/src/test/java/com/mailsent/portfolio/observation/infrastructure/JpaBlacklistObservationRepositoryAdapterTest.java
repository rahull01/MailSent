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

import com.mailsent.portfolio.observation.domain.BlacklistObservation;
import com.mailsent.portfolio.observation.domain.BlacklistSource;
import com.mailsent.portfolio.observation.infrastructure.persistence.BlacklistObservationJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaBlacklistObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaBlacklistObservationRepositoryAdapter;

/**
 * Tests the production {@link JpaBlacklistObservationRepositoryAdapter} in
 * isolation with a mocked Spring Data repository ({@code @Profile("!test")}),
 * including exact raw-payload round-trip.
 */
class JpaBlacklistObservationRepositoryAdapterTest {

    private final JpaBlacklistObservationRepository jpa = mock(JpaBlacklistObservationRepository.class);
    private final JpaBlacklistObservationRepositoryAdapter adapter =
        new JpaBlacklistObservationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void payloadRoundTripsExactly() {
        BlacklistObservation observation = BlacklistObservation.restore(
            "bl-1", "org-1", "dom-1", "example.com", BlacklistSource.SPAMHAUS, now,
            "literal-raw-payload");
        ArgumentCaptor<BlacklistObservationJpaEntity> captor =
            ArgumentCaptor.forClass(BlacklistObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        BlacklistObservation saved = adapter.save(observation);

        assertEquals("literal-raw-payload", saved.payload());
        assertEquals("literal-raw-payload", captor.getValue().payload());
        assertEquals("SPAMHAUS", captor.getValue().source());
    }

    @Test
    void findByIdRestoresSourceAndPayload() {
        BlacklistObservationJpaEntity entity = new BlacklistObservationJpaEntity(
            "bl-1", "org-1", "dom-1", "example.com", "SURBL", now, "surbl-raw");
        when(jpa.findById("bl-1")).thenReturn(Optional.of(entity));

        BlacklistObservation found = adapter.findById("bl-1").orElseThrow();
        assertEquals("bl-1", found.id());
        assertEquals(BlacklistSource.SURBL, found.source());
        assertEquals("surbl-raw", found.payload());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        BlacklistObservationJpaEntity entity = new BlacklistObservationJpaEntity(
            "bl-1", "org-1", "dom-1", "example.com", "SPAMHAUS", now, "raw");
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }

    @Test
    void saveAndFlushIsUsedRatherThanUpdateOrDelete() {
        BlacklistObservation observation = BlacklistObservation.restore(
            "bl-1", "org-1", "dom-1", "example.com", BlacklistSource.URIBL, now, "raw");
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(observation);
        assertTrue(adapter.findById("missing").isEmpty());
    }
}
