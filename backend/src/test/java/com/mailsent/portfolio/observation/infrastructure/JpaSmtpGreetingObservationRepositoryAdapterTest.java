package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;
import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaSmtpGreetingObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaSmtpGreetingObservationRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.SmtpGreetingObservationJpaEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the production {@link JpaSmtpGreetingObservationRepositoryAdapter} in
 * isolation with a mocked Spring Data repository ({@code @Profile("!test")}),
 * including TEXT round-trip of greeting and EHLO.
 */
class JpaSmtpGreetingObservationRepositoryAdapterTest {

    private final JpaSmtpGreetingObservationRepository jpa = mock(JpaSmtpGreetingObservationRepository.class);
    private final JpaSmtpGreetingObservationRepositoryAdapter adapter =
        new JpaSmtpGreetingObservationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void greetingAndEhloTextRoundTripThroughAdapter() {
        SmtpGreetingObservation observation = SmtpGreetingObservation.restore(
            "smtp-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            IpAddressFamily.IPV4, SmtpProbeOutcome.CONNECTED, "220 mail ESMTP", "250 HELP", now);
        ArgumentCaptor<SmtpGreetingObservationJpaEntity> captor =
            ArgumentCaptor.forClass(SmtpGreetingObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        SmtpGreetingObservation saved = adapter.save(observation);

        assertEquals("220 mail ESMTP", saved.rawGreeting());
        assertEquals("250 HELP", saved.rawEhlo());
        assertEquals("220 mail ESMTP", captor.getValue().rawGreeting());
        assertEquals("250 HELP", captor.getValue().rawEhlo());
        assertEquals("CONNECTED", captor.getValue().probeOutcome());
        assertEquals("IPV4", captor.getValue().addressFamily());
    }

    @Test
    void emptyGreetingRoundTripsAsEmptyTextNotFabricatedBanner() {
        SmtpGreetingObservation observation = SmtpGreetingObservation.restore(
            "smtp-1", "org-1", "dom-1", "example.com", "mail.example.com", "2001:db8::1",
            IpAddressFamily.IPV6, SmtpProbeOutcome.CONNECTED, "", "", now);
        ArgumentCaptor<SmtpGreetingObservationJpaEntity> captor =
            ArgumentCaptor.forClass(SmtpGreetingObservationJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        SmtpGreetingObservation saved = adapter.save(observation);

        assertEquals("", saved.rawGreeting());
        assertEquals("", captor.getValue().rawGreeting());
        assertEquals("IPV6", captor.getValue().addressFamily());
    }

    @Test
    void findByIdDeserializesStoredText() {
        SmtpGreetingObservationJpaEntity entity = new SmtpGreetingObservationJpaEntity(
            "smtp-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            "IPV4", "CONNECTED", "220 mail ESMTP", "250 HELP", now);
        when(jpa.findById("smtp-1")).thenReturn(Optional.of(entity));

        SmtpGreetingObservation found = adapter.findById("smtp-1").orElseThrow();
        assertEquals("smtp-1", found.id());
        assertEquals(IpAddressFamily.IPV4, found.addressFamily());
        assertEquals(SmtpProbeOutcome.CONNECTED, found.probeOutcome());
        assertEquals("220 mail ESMTP", found.rawGreeting());
        assertEquals("mail.example.com", found.sourceMxHostname());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        SmtpGreetingObservationJpaEntity entity = new SmtpGreetingObservationJpaEntity(
            "smtp-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            "IPV4", "CONNECTED", "", null, now);
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }

    @Test
    void saveAndFlushIsUsedRatherThanUpdateOrDelete() {
        SmtpGreetingObservation observation = SmtpGreetingObservation.restore(
            "smtp-1", "org-1", "dom-1", "example.com", "mail.example.com", "192.0.2.10",
            IpAddressFamily.IPV4, SmtpProbeOutcome.CONNECTED, "", "", now);
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(observation);
        assertTrue(adapter.findById("missing").isEmpty());
    }
}
