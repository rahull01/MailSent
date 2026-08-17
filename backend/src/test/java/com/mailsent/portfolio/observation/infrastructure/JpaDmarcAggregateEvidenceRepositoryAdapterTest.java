package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.infrastructure.persistence.DmarcAggregateEvidenceJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcAggregateEvidenceRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcAggregateReportOccupancyRepository;

/**
 * Tests the production {@link JpaDmarcAggregateEvidenceRepositoryAdapter} in
 * isolation with a mocked Spring Data repository ({@code @Profile("!test")}),
 * including dkim_domains JSON round-trip.
 */
class JpaDmarcAggregateEvidenceRepositoryAdapterTest {

    private final JpaDmarcAggregateEvidenceRepository jpa = mock(JpaDmarcAggregateEvidenceRepository.class);
    private final JpaDmarcAggregateReportOccupancyRepository occupancy =
        mock(JpaDmarcAggregateReportOccupancyRepository.class);
    private final JpaDmarcAggregateEvidenceRepositoryAdapter adapter =
        new JpaDmarcAggregateEvidenceRepositoryAdapter(jpa, occupancy);
    private final LocalDate day = LocalDate.of(2021, 1, 1);

    @Test
    void dkimDomainsRoundTripAsJsonArray() {
        DmarcAggregateEvidence snapshot = DmarcAggregateEvidence.restore(
            "agg-1", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.1",
            day, 3L, "fail", "pass", "fail", "pass", "bounce.mail.example", "example.com",
            List.of("sel.example.com", "ex.example.com"), "none");
        ArgumentCaptor<DmarcAggregateEvidenceJpaEntity> captor =
            ArgumentCaptor.forClass(DmarcAggregateEvidenceJpaEntity.class);
        when(jpa.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        DmarcAggregateEvidence saved = adapter.save(snapshot);

        assertEquals(List.of("sel.example.com", "ex.example.com"), saved.dkimDomains());
        assertEquals("[\"sel.example.com\",\"ex.example.com\"]", captor.getValue().dkimDomains());
        assertEquals(3L, captor.getValue().messageCount());
        assertEquals(day, captor.getValue().utcDay());
    }

    @Test
    void findByIdRestoresFacts() {
        DmarcAggregateEvidenceJpaEntity entity = new DmarcAggregateEvidenceJpaEntity(
            "agg-1", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.1",
            day, 3L, "fail", "pass", "fail", "pass", "bounce.mail.example", "example.com",
            "[\"sel.example.com\"]", "none");
        when(jpa.findById("agg-1")).thenReturn(Optional.of(entity));

        DmarcAggregateEvidence found = adapter.findById("agg-1").orElseThrow();
        assertEquals("agg-1", found.id());
        assertEquals("rid-1", found.reportId());
        assertEquals(List.of("sel.example.com"), found.dkimDomains());
        assertEquals(3L, found.count());
    }

    @Test
    void findByDomainOrganizationAndReportDelegateToSpringData() {
        DmarcAggregateEvidenceJpaEntity entity = new DmarcAggregateEvidenceJpaEntity(
            "agg-1", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.1",
            day, 1L, "fail", "pass", "fail", "pass", null, "example.com", "[]", "none");
        when(jpa.findByDomainId("dom-1")).thenReturn(List.of(entity));
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(entity));
        when(jpa.findByDomainIdAndReportId("dom-1", "rid-1")).thenReturn(List.of(entity));

        assertEquals(1, adapter.findByDomainId("dom-1").size());
        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals(1, adapter.findByDomainIdAndReportId("dom-1", "rid-1").size());
        assertEquals("example.com", adapter.findByDomainId("dom-1").get(0).domainName());
    }

    @Test
    void saveAndFlushIsUsedRatherThanUpdateOrDelete() {
        DmarcAggregateEvidence snapshot = DmarcAggregateEvidence.restore(
            "agg-1", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.1",
            day, 1L, null, null, null, null, null, null, List.of(), null);
        when(jpa.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(snapshot);
        assertTrue(adapter.findById("missing").isEmpty());
    }
}
