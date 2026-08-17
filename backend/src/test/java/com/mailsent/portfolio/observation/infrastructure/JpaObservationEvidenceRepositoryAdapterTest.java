package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaObservationEvidenceRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.ObservationEvidenceJpaEntity;

/**
 * Tests the production {@link JpaObservationEvidenceRepositoryAdapter} in isolation with a
 * mocked Spring Data repository ({@code @Profile("!test")}), including Jackson JSON
 * round-trip of MX and TXT lists.
 */
class JpaObservationEvidenceRepositoryAdapterTest {

    private final JpaObservationEvidenceRepository jpa = mock(JpaObservationEvidenceRepository.class);
    private final JpaObservationEvidenceRepositoryAdapter adapter = new JpaObservationEvidenceRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void mxJsonRoundTripsThroughAdapter() {
        ObservationEvidence evidence = ObservationEvidence.restore(
            "ev-1", "org-1", "dom-1", "example.com", now,
            List.of(new MxRecord(10, "mail.example.com"), new MxRecord(20, "backup.example.com")),
            List.of(), null);
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ObservationEvidence saved = adapter.save(evidence);

        assertEquals(2, saved.mxRecords().size());
        assertEquals(10, saved.mxRecords().get(0).priority());
        assertEquals("mail.example.com", saved.mxRecords().get(0).exchange());
        assertEquals(20, saved.mxRecords().get(1).priority());
        assertEquals("backup.example.com", saved.mxRecords().get(1).exchange());
    }

    @Test
    void txtJsonRoundTripsThroughAdapter() {
        ObservationEvidence evidence = ObservationEvidence.restore(
            "ev-1", "org-1", "dom-1", "example.com", now,
            List.of(), List.of("v=spf1 -all", "google-site-verification=abc"), "v=spf1 -all");
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ObservationEvidence saved = adapter.save(evidence);

        assertEquals(List.of("v=spf1 -all", "google-site-verification=abc"), saved.txtRecords());
        assertEquals("v=spf1 -all", saved.spfRecord().orElseThrow());
    }

    @Test
    void emptyListsRoundTripAsJsonArrays() {
        ObservationEvidence evidence = ObservationEvidence.restore(
            "ev-1", "org-1", "dom-1", "example.com", now, List.of(), List.of(), null);
        ArgumentCaptor<ObservationEvidenceJpaEntity> captor =
            ArgumentCaptor.forClass(ObservationEvidenceJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ObservationEvidence saved = adapter.save(evidence);

        assertTrue(saved.mxRecords().isEmpty());
        assertTrue(saved.txtRecords().isEmpty());
        assertEquals("[]", captor.getValue().mxRecords());
        assertEquals("[]", captor.getValue().txtRecords());
        assertNull(captor.getValue().spfRecord());
    }

    @Test
    void findByIdDeserializesStoredJson() {
        ObservationEvidenceJpaEntity entity = new ObservationEvidenceJpaEntity(
            "ev-1", "org-1", "dom-1", "example.com", now,
            "[{\"priority\":10,\"exchange\":\"mail.example.com\"}]",
            "[\"v=spf1 -all\"]",
            "v=spf1 -all");
        when(jpa.findById("ev-1")).thenReturn(Optional.of(entity));

        ObservationEvidence found = adapter.findById("ev-1").orElseThrow();
        assertEquals("ev-1", found.id());
        assertEquals(List.of(new MxRecord(10, "mail.example.com")), found.mxRecords());
        assertEquals(List.of("v=spf1 -all"), found.txtRecords());
    }

    @Test
    void findByDomainIdAndOrganizationIdDelegateToSpringData() {
        ObservationEvidenceJpaEntity entity = new ObservationEvidenceJpaEntity(
            "ev-1", "org-1", "dom-1", "example.com", now, "[]", "[]", null);
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }
}
