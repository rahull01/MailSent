package com.mailsent.monitoring.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.monitoring.domain.Incident;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaIncidentRepositoryAdapter.class)
class JpaIncidentRepositoryAdapterTest {

    @Autowired
    private JpaIncidentRepositoryAdapter adapter;

    @Test
    void savesAndFindsActiveIncident() {
        Incident incident = Incident.open("dom-1", Instant.now());
        adapter.save(incident);
        
        Optional<Incident> active = adapter.findActiveByDomainId("dom-1");
        
        assertTrue(active.isPresent());
        assertEquals(incident.id(), active.get().id());
        assertFalse(active.get().isResolved());
    }

    @Test
    void findsById() {
        Incident incident = Incident.open("dom-2", Instant.now());
        adapter.save(incident);
        
        Optional<Incident> found = adapter.findById(incident.id());
        
        assertTrue(found.isPresent());
        assertEquals("dom-2", found.get().domainId());
    }

    @Test
    void noActiveIncidentWhenResolved() {
        Incident incident = Incident.open("dom-3", Instant.now());
        incident.resolve(Instant.now().plusSeconds(60));
        adapter.save(incident);
        
        Optional<Incident> active = adapter.findActiveByDomainId("dom-3");
        
        assertFalse(active.isPresent());
    }
}
