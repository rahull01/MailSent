package com.mailsent.evidence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class EvidenceExportTest {

    @Test
    void requestsValidExport() {
        String targetId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        EvidenceExport export = EvidenceExport.request(
            targetId, 
            userId, 
            EvidenceExport.ExportFormat.JSON, 
            now
        );
        
        assertNotNull(export.id());
        assertEquals(targetId, export.targetId());
        assertEquals(userId, export.requestingUserId());
        assertEquals(EvidenceExport.ExportFormat.JSON, export.format());
        assertEquals(now, export.requestedAt());
    }

    @Test
    void rejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> 
            EvidenceExport.request(null, "user", EvidenceExport.ExportFormat.CSV, Instant.now())
        );
        assertThrows(NullPointerException.class, () -> 
            EvidenceExport.request("target", null, EvidenceExport.ExportFormat.CSV, Instant.now())
        );
        assertThrows(NullPointerException.class, () -> 
            EvidenceExport.request("target", "user", null, Instant.now())
        );
    }
}
