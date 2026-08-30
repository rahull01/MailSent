package com.mailsent.monitoring.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;

/**
 * Unit tests for {@link JpaHealthChangeEventRepositoryAdapter} in isolation using a mocked
 * Spring Data repository, matching the hermetic DDD adapter test pattern across bounded contexts.
 */
class JpaHealthChangeEventRepositoryAdapterTest {

    private final SpringDataHealthChangeEventRepository repo = mock(SpringDataHealthChangeEventRepository.class);
    private final JpaHealthChangeEventRepositoryAdapter adapter = new JpaHealthChangeEventRepositoryAdapter(repo);

    @Test
    void savesEventMappingToEntity() {
        Instant now = Instant.now();
        HealthChangeEvent event = HealthChangeEvent.detect("org-1", "dom-1", null, HealthIndication.HEALTHY, now);

        adapter.save(event);

        ArgumentCaptor<HealthChangeEventEntity> captor = ArgumentCaptor.forClass(HealthChangeEventEntity.class);
        verify(repo).save(captor.capture());

        HealthChangeEventEntity entity = captor.getValue();
        assertEquals(event.id(), entity.getId());
        assertEquals("org-1", entity.getOrganizationId());
        assertEquals("dom-1", entity.getDomainId());
        assertEquals(HealthIndication.HEALTHY, entity.getCurrentIndication());
        assertEquals(now, entity.getDetectedAt());
    }

    @Test
    void findsEventsByOrganizationIdAndDomainId() {
        Instant now = Instant.now();
        HealthChangeEventEntity e1 = new HealthChangeEventEntity();
        e1.setId("e-1");
        e1.setOrganizationId("org-1");
        e1.setDomainId("dom-1");
        e1.setPreviousIndication(HealthIndication.HEALTHY);
        e1.setCurrentIndication(HealthIndication.AT_RISK);
        e1.setDetectedAt(now.plusSeconds(10));

        HealthChangeEventEntity e2 = new HealthChangeEventEntity();
        e2.setId("e-2");
        e2.setOrganizationId("org-1");
        e2.setDomainId("dom-1");
        e2.setPreviousIndication(null);
        e2.setCurrentIndication(HealthIndication.HEALTHY);
        e2.setDetectedAt(now);

        when(repo.findByOrganizationIdAndDomainIdOrderByDetectedAtDesc("org-1", "dom-1"))
            .thenReturn(List.of(e1, e2));

        List<HealthChangeEvent> events = adapter.findByOrganizationIdAndDomainId("org-1", "dom-1");

        assertEquals(2, events.size());
        assertEquals("org-1", events.get(0).organizationId());
        assertEquals("dom-1", events.get(0).domainId());
        assertEquals(HealthIndication.AT_RISK, events.get(0).currentIndication());
        assertEquals(HealthIndication.HEALTHY, events.get(1).currentIndication());
    }
}
