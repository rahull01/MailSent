package com.mailsent.resolution.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.resolution.domain.ResolutionAction;

class ResolutionServiceTest {

    private final ResolutionService service = new ResolutionService();

    @Test
    void generatesActionsForAtRisk() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-1", List.of(), List.of(), Instant.now());
            
        List<ResolutionAction> actions = service.generateActions(signal);
        
        assertEquals(2, actions.size());
        assertEquals(1, actions.get(0).priority());
        assertTrue(actions.get(0).title().contains("Identify failing sender"));
        assertTrue(actions.get(0).exactSteps().get(0).contains("example.com"));
        assertEquals(2, actions.get(1).priority());
        assertTrue(actions.get(1).title().contains("Fix DNS"));
    }

    @Test
    void generatesActionsForUnknown() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.UNKNOWN, "oe-1", List.of(), List.of(), Instant.now());
            
        List<ResolutionAction> actions = service.generateActions(signal);
        
        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).priority());
        assertTrue(actions.get(0).title().contains("Publish DMARC record"));
    }

    @Test
    void generatesNoActionsForHealthy() {
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of(), List.of(), Instant.now());
            
        List<ResolutionAction> actions = service.generateActions(signal);
        
        assertTrue(actions.isEmpty());
    }

    @Test
    void requiresSignal() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, () -> service.generateActions(null)
        );
        assertEquals("Health signal is required to generate resolution actions", exception.getMessage());
    }
}
