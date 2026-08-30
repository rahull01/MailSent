package com.mailsent.monitoring.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mailsent.monitoring.domain.Incident;

/**
 * Unit tests for {@link JpaIncidentRepositoryAdapter} in isolation using a mocked
 * Spring Data repository, matching the hermetic DDD adapter test pattern across bounded contexts.
 */
class JpaIncidentRepositoryAdapterTest {

    private final SpringDataIncidentRepository repo = mock(SpringDataIncidentRepository.class);
    private final JpaIncidentRepositoryAdapter adapter = new JpaIncidentRepositoryAdapter(repo);

    @Test
    void savesIncidentMappingToEntity() {
        Instant now = Instant.now();
        Incident incident = Incident.open("org-1", "dom-1", now);

        adapter.save(incident);

        ArgumentCaptor<IncidentEntity> captor = ArgumentCaptor.forClass(IncidentEntity.class);
        verify(repo).save(captor.capture());

        IncidentEntity entity = captor.getValue();
        assertEquals(incident.id(), entity.getId());
        assertEquals("org-1", entity.getOrganizationId());
        assertEquals("dom-1", entity.getDomainId());
        assertEquals(now, entity.getOpenedAt());
    }

    @Test
    void findsActiveIncidentByOrganizationIdAndDomainId() {
        Instant now = Instant.now();
        IncidentEntity entity = new IncidentEntity();
        entity.setId("inc-1");
        entity.setOrganizationId("org-1");
        entity.setDomainId("dom-1");
        entity.setOpenedAt(now);
        entity.setResolvedAt(null);

        when(repo.findByOrganizationIdAndDomainIdAndResolvedAtIsNull("org-1", "dom-1"))
            .thenReturn(Optional.of(entity));

        Optional<Incident> active = adapter.findActiveByOrganizationIdAndDomainId("org-1", "dom-1");

        assertTrue(active.isPresent());
        assertEquals("inc-1", active.get().id());
        assertEquals("org-1", active.get().organizationId());
        assertEquals("dom-1", active.get().domainId());
        assertFalse(active.get().isResolved());
    }

    @Test
    void findsByIdScopedToOrganization() {
        Instant now = Instant.now();
        IncidentEntity entity = new IncidentEntity();
        entity.setId("inc-2");
        entity.setOrganizationId("org-1");
        entity.setDomainId("dom-2");
        entity.setOpenedAt(now);

        when(repo.findByOrganizationIdAndId("org-1", "inc-2"))
            .thenReturn(Optional.of(entity));

        Optional<Incident> found = adapter.findById("org-1", "inc-2");

        assertTrue(found.isPresent());
        assertEquals("inc-2", found.get().id());
        assertEquals("org-1", found.get().organizationId());
        assertEquals("dom-2", found.get().domainId());
    }

    @Test
    void returnsEmptyWhenNoActiveIncident() {
        when(repo.findByOrganizationIdAndDomainIdAndResolvedAtIsNull("org-1", "dom-3"))
            .thenReturn(Optional.empty());

        Optional<Incident> active = adapter.findActiveByOrganizationIdAndDomainId("org-1", "dom-3");

        assertFalse(active.isPresent());
    }
}
