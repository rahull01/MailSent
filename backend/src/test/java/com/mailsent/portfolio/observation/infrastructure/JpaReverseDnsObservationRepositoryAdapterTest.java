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

import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaReverseDnsObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaReverseDnsObservationRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.ReverseDnsObservationJpaEntity;

/**
 * Tests the production {@link JpaReverseDnsObservationRepositoryAdapter} in
 * isolation with a mocked Spring Data repository ({@code @Profile("!test")}),
 * including TEXT JSON round-trip of PTR values.
 */
class JpaReverseDnsObservationRepositoryAdapterTest {

    private final JpaReverseDnsObservationRepository jpa = mock(JpaReverseDnsObservationRepository.class);
    private final JpaReverseDnsObservationRepositoryAdapter adapter =
        new JpaReverseDnsObservationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void ptrValuesJsonRoundTripThroughAdapter() {
        ReverseDnsObservation observation = ReverseDnsObservation.restore(
            "rdns-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            IpAddressFamily.IPV4, List.of("ptr.example.net", "alt.example.net"), now);
        ArgumentCaptor<ReverseDnsObservationJpaEntity> captor =
            ArgumentCaptor.forClass(ReverseDnsObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ReverseDnsObservation saved = adapter.save(observation);

        assertEquals(List.of("ptr.example.net", "alt.example.net"), saved.ptrValues());
        assertEquals("[\"ptr.example.net\",\"alt.example.net\"]", captor.getValue().ptrValues());
        assertEquals("IPV4", captor.getValue().addressFamily());
        assertEquals("192.0.2.10", captor.getValue().ip());
    }

    @Test
    void emptyPtrListRoundTripsAsJsonArray() {
        ReverseDnsObservation observation = ReverseDnsObservation.restore(
            "rdns-1", "org-1", "dom-1", "example.com", "mail.example.com", "2001:db8::1",
            IpAddressFamily.IPV6, List.of(), now);
        ArgumentCaptor<ReverseDnsObservationJpaEntity> captor =
            ArgumentCaptor.forClass(ReverseDnsObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ReverseDnsObservation saved = adapter.save(observation);

        assertTrue(saved.ptrValues().isEmpty());
        assertEquals("[]", captor.getValue().ptrValues());
        assertEquals("IPV6", captor.getValue().addressFamily());
    }

    @Test
    void findByIdDeserializesStoredJson() {
        ReverseDnsObservationJpaEntity entity = new ReverseDnsObservationJpaEntity(
            "rdns-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            "IPV4", "[\"ptr.example.net\"]", now);
        when(jpa.findById("rdns-1")).thenReturn(Optional.of(entity));

        ReverseDnsObservation found = adapter.findById("rdns-1").orElseThrow();
        assertEquals("rdns-1", found.id());
        assertEquals(IpAddressFamily.IPV4, found.addressFamily());
        assertEquals(List.of("ptr.example.net"), found.ptrValues());
        assertEquals("mail.example.com", found.sourceMxHostname());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        ReverseDnsObservationJpaEntity entity = new ReverseDnsObservationJpaEntity(
            "rdns-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            "IPV4", "[]", now);
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }

    @Test
    void saveAndFlushIsUsedRatherThanUpdateOrDelete() {
        ReverseDnsObservation observation = ReverseDnsObservation.restore(
            "rdns-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            IpAddressFamily.IPV4, List.of(), now);
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(observation);
        assertTrue(adapter.findById("missing").isEmpty());
    }
}
