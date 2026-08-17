package com.mailsent.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AiRecommendationTest {

    @Test
    void proposesValidRecommendationWithConfidence() {
        String issueId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        AiRecommendation rec = AiRecommendation.propose(
            issueId, 
            "Update DNS TXT record for SPF to include v=spf1 include:_spf.google.com ~all", 
            98, 
            now
        );
        
        assertNotNull(rec.id());
        assertEquals(issueId, rec.targetIssueId());
        assertEquals("Update DNS TXT record for SPF to include v=spf1 include:_spf.google.com ~all", rec.recommendedAction());
        assertEquals(98, rec.confidenceScore());
        assertEquals(now, rec.generatedAt());
    }

    @Test
    void rejectsInvalidConfidenceScores() {
        assertThrows(IllegalArgumentException.class, () -> 
            AiRecommendation.propose("issue-1", "Action", -1, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () -> 
            AiRecommendation.propose("issue-1", "Action", 101, Instant.now())
        );
    }

    @Test
    void rejectsEmptyAction() {
        assertThrows(IllegalArgumentException.class, () -> 
            AiRecommendation.propose("issue-1", "   ", 80, Instant.now())
        );
    }
}
