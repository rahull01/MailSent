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

import com.mailsent.portfolio.observation.domain.DnsHealthObservation;
import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.DnssecPresence;
import com.mailsent.portfolio.observation.domain.PropagationAnswer;
import com.mailsent.portfolio.observation.domain.PropagationObservation;
import com.mailsent.portfolio.observation.domain.TtlObservation;
import com.mailsent.portfolio.observation.infrastructure.persistence.DnsHealthObservationJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDnsHealthObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDnsHealthObservationRepositoryAdapter;

/**
 * Tests the production {@link JpaDnsHealthObservationRepositoryAdapter} in
 * isolation with a mocked Spring Data repository ({@code @Profile("!test")}),
 * including Jackson JSON round-trip of TTL, DNSSEC, and propagation lists.
 */
class JpaDnsHealthObservationRepositoryAdapterTest {

    private final JpaDnsHealthObservationRepository jpa = mock(JpaDnsHealthObservationRepository.class);
    private final JpaDnsHealthObservationRepositoryAdapter adapter =
        new JpaDnsHealthObservationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void ttlDnssecAndPropagationJsonRoundTripThroughAdapter() {
        DnsHealthObservation observation = DnsHealthObservation.restore(
            "dns-1", "org-1", "dom-1", "example.com", now,
            List.of(new TtlObservation("MX", "example.com", "10 mail.example.com.", 3600)),
            List.of(new DnssecObservation("DNSKEY", DnssecPresence.ABSENT, List.of())),
            List.of(new PropagationObservation("system", "MX", "example.com", true,
                List.of(new PropagationAnswer("10 mail.example.com.", 3600)))));
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DnsHealthObservation saved = adapter.save(observation);

        assertEquals(1, saved.ttlObservations().size());
        assertEquals(3600L, saved.ttlObservations().get(0).ttlSeconds());
        assertEquals(DnssecPresence.ABSENT, saved.dnssecObservations().get(0).presence());
        assertEquals("system", saved.propagationObservations().get(0).resolverIdentity());
        assertEquals("10 mail.example.com.", saved.propagationObservations().get(0).answers().get(0).rdata());
    }

    @Test
    void emptyListsRoundTripAsJsonArrays() {
        DnsHealthObservation observation = DnsHealthObservation.restore(
            "dns-1", "org-1", "dom-1", "example.com", now, List.of(), List.of(), List.of());
        ArgumentCaptor<DnsHealthObservationJpaEntity> captor =
            ArgumentCaptor.forClass(DnsHealthObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        DnsHealthObservation saved = adapter.save(observation);

        assertTrue(saved.ttlObservations().isEmpty());
        assertTrue(saved.dnssecObservations().isEmpty());
        assertTrue(saved.propagationObservations().isEmpty());
        assertEquals("[]", captor.getValue().ttlObservations());
        assertEquals("[]", captor.getValue().dnssecObservations());
        assertEquals("[]", captor.getValue().propagationObservations());
    }

    @Test
    void findByIdDeserializesStoredJson() {
        DnsHealthObservationJpaEntity entity = new DnsHealthObservationJpaEntity(
            "dns-1", "org-1", "dom-1", "example.com", now,
            "[{\"recordType\":\"MX\",\"owner\":\"example.com\",\"rdata\":\"10 mail.example.com.\",\"ttlSeconds\":3600}]",
            "[{\"recordType\":\"DNSKEY\",\"presence\":\"ABSENT\",\"records\":[]}]",
            "[{\"resolverIdentity\":\"8.8.8.8\",\"queryType\":\"MX\",\"owner\":\"example.com\",\"available\":false,\"answers\":[]}]");
        when(jpa.findById("dns-1")).thenReturn(Optional.of(entity));

        DnsHealthObservation found = adapter.findById("dns-1").orElseThrow();
        assertEquals("dns-1", found.id());
        assertEquals("example.com", found.domainName());
        assertEquals("MX", found.ttlObservations().get(0).recordType());
        assertEquals(DnssecPresence.ABSENT, found.dnssecObservations().get(0).presence());
        assertTrue(!found.propagationObservations().get(0).available());
        assertTrue(found.propagationObservations().get(0).answers().isEmpty());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        DnsHealthObservationJpaEntity entity = new DnsHealthObservationJpaEntity(
            "dns-1", "org-1", "dom-1", "example.com", now, "[]", "[]", "[]");
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }
}
