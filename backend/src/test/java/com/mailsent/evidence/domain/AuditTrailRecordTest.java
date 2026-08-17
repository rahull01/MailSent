package com.mailsent.evidence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuditTrailRecordTest {

    @Test
    void recordsValidAuditTrail() {
        String sourceId = UUID.randomUUID().toString();
        String conclusionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        AuditTrailRecord record = AuditTrailRecord.record(
            "HEALTH_SIGNAL_GENERATION", 
            sourceId, 
            conclusionId, 
            now
        );
        
        assertNotNull(record.id());
        assertEquals("HEALTH_SIGNAL_GENERATION", record.actionName());
        assertEquals(sourceId, record.sourceEvidenceId());
        assertEquals(conclusionId, record.resultingConclusionId());
        assertEquals(now, record.recordedAt());
    }

    @Test
    void rejectsEmptyLineageProperties() {
        assertThrows(IllegalArgumentException.class, () -> 
            AuditTrailRecord.record(" ", "sourceId", "conclusionId", Instant.now())
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            AuditTrailRecord.record("ACTION", "", "conclusionId", Instant.now())
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            AuditTrailRecord.record("ACTION", "sourceId", "  ", Instant.now())
        );
    }
}
