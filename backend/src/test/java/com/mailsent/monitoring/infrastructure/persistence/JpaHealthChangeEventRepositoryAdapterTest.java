package com.mailsent.monitoring.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaHealthChangeEventRepositoryAdapter.class)
class JpaHealthChangeEventRepositoryAdapterTest {

    @Autowired
    private JpaHealthChangeEventRepositoryAdapter adapter;

    @Test
    void savesAndFindsEventsByDomainId() {
        Instant now = Instant.now();
        HealthChangeEvent event1 = HealthChangeEvent.detect("dom-1", null, HealthIndication.HEALTHY, now);
        HealthChangeEvent event2 = HealthChangeEvent.detect("dom-1", HealthIndication.HEALTHY, HealthIndication.AT_RISK, now.plusSeconds(10));
        
        adapter.save(event1);
        adapter.save(event2);
        
        List<HealthChangeEvent> events = adapter.findByDomainId("dom-1");
        
        assertEquals(2, events.size());
        assertEquals(HealthIndication.AT_RISK, events.get(0).currentIndication()); // Descending by date
        assertEquals(HealthIndication.HEALTHY, events.get(1).currentIndication());
    }
}
