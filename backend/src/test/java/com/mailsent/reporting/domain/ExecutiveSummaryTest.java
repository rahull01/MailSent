package com.mailsent.reporting.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ExecutiveSummaryTest {

    @Test
    void generatesValid3BulletSummary() {
        String targetId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        ExecutiveSummary summary = ExecutiveSummary.generate(
            targetId, 
            "Deliverability fell 20% this week.", 
            "Authentication failures spiked on Tuesday.", 
            "Fix SPF records for your marketing domain.", 
            now
        );
        
        assertNotNull(summary.id());
        assertEquals(targetId, summary.targetId());
        assertEquals("Deliverability fell 20% this week.", summary.primaryFinding());
        assertEquals("Authentication failures spiked on Tuesday.", summary.secondaryFinding());
        assertEquals("Fix SPF records for your marketing domain.", summary.recommendation());
        assertEquals(now, summary.generatedAt());
    }

    @Test
    void rejectsIncompleteBullets() {
        assertThrows(IllegalArgumentException.class, () -> 
            ExecutiveSummary.generate(UUID.randomUUID().toString(), " ", "Bullet 2", "Bullet 3", Instant.now())
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            ExecutiveSummary.generate(UUID.randomUUID().toString(), "Bullet 1", "", "Bullet 3", Instant.now())
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            ExecutiveSummary.generate(UUID.randomUUID().toString(), "Bullet 1", "Bullet 2", "   ", Instant.now())
        );
    }
}
