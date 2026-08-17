package com.mailsent.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AiFeedbackTest {

    @Test
    void submitsValidFeedback() {
        String targetId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        AiFeedback feedback = AiFeedback.submit(
            targetId, 
            userId, 
            AiFeedback.FeedbackRating.INACCURATE, 
            "The recommended DNS record is malformed.", 
            now
        );
        
        assertNotNull(feedback.id());
        assertEquals(targetId, feedback.aiGeneratedTargetId());
        assertEquals(userId, feedback.userId());
        assertEquals(AiFeedback.FeedbackRating.INACCURATE, feedback.rating());
        assertEquals("The recommended DNS record is malformed.", feedback.userComment());
        assertEquals(now, feedback.submittedAt());
    }

    @Test
    void acceptsNullComment() {
        AiFeedback feedback = AiFeedback.submit(
            UUID.randomUUID().toString(), 
            UUID.randomUUID().toString(), 
            AiFeedback.FeedbackRating.HELPFUL, 
            null, 
            Instant.now()
        );
        assertNull(feedback.userComment());
    }

    @Test
    void rejectsEmptyIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> 
            AiFeedback.submit("   ", "user-1", AiFeedback.FeedbackRating.HELPFUL, null, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () -> 
            AiFeedback.submit("target-1", "   ", AiFeedback.FeedbackRating.HELPFUL, null, Instant.now())
        );
    }
}
