package com.mailsent.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class InAppUsefulnessFeedbackTest {

    @Test
    void recordsFeedbackSuccessfully() {
        Instant now = Instant.now();
        InAppUsefulnessFeedback feedback = InAppUsefulnessFeedback.record(
                "org-1",
                "usr-1",
                FeedbackTargetType.EXPLANATION,
                "exp-123",
                true,
                "Very clear next steps",
                now
        );

        assertNotNull(feedback.id());
        assertEquals("org-1", feedback.organizationId());
        assertEquals("usr-1", feedback.userId());
        assertEquals(FeedbackTargetType.EXPLANATION, feedback.targetType());
        assertEquals("exp-123", feedback.targetId());
        assertTrue(feedback.isUseful());
        assertEquals("Very clear next steps", feedback.comment());
        assertEquals(now, feedback.submittedAt());
    }

    @Test
    void validatesBlankInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                InAppUsefulnessFeedback.record("", "usr-1", FeedbackTargetType.GUIDANCE, "tgt-1", true, null, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                InAppUsefulnessFeedback.record("org-1", "", FeedbackTargetType.GUIDANCE, "tgt-1", true, null, Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                InAppUsefulnessFeedback.record("org-1", "usr-1", FeedbackTargetType.GUIDANCE, "", true, null, Instant.now())
        );
    }
}
