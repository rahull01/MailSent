package com.mailsent.evidence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class EvidenceTraceTest {

    @Test
    void linksValidEvidenceTrace() {
        String reportId = UUID.randomUUID().toString();
        String observationId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        EvidenceTrace trace = EvidenceTrace.link(
            reportId, 
            observationId, 
            "Authentication failed due to missing SPF", 
            now
        );
        
        assertNotNull(trace.id());
        assertEquals(reportId, trace.reportId());
        assertEquals(observationId, trace.observationId());
        assertEquals("Authentication failed due to missing SPF", trace.findingAssertion());
        assertEquals(now, trace.tracedAt());
    }

    @Test
    void rejectsEmptyFindingAssertion() {
        assertThrows(IllegalArgumentException.class, () -> 
            EvidenceTrace.link(
                UUID.randomUUID().toString(), 
                UUID.randomUUID().toString(), 
                "   ", 
                Instant.now()
            )
        );
    }
}
