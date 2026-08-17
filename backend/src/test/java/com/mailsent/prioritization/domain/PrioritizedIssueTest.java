package com.mailsent.prioritization.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PrioritizedIssueTest {

    @Test
    void ranksIssueValidly() {
        String findingId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        PrioritizedIssue issue = PrioritizedIssue.rank(
            findingId, 
            95, 
            "Authentication failures are actively blocking delivery to inbox.", 
            now
        );
        
        assertNotNull(issue.id());
        assertEquals(findingId, issue.sourceFindingId());
        assertEquals(95, issue.priorityScore());
        assertEquals("Authentication failures are actively blocking delivery to inbox.", issue.aiJustification());
        assertEquals(now, issue.prioritizedAt());
    }

    @Test
    void rejectsInvalidScores() {
        assertThrows(IllegalArgumentException.class, () -> 
            PrioritizedIssue.rank("finding-1", -1, "Justification", Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () -> 
            PrioritizedIssue.rank("finding-1", 101, "Justification", Instant.now())
        );
    }

    @Test
    void rejectsEmptyJustification() {
        assertThrows(IllegalArgumentException.class, () -> 
            PrioritizedIssue.rank("finding-1", 50, "   ", Instant.now())
        );
    }
}
