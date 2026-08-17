package com.mailsent.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class GroundedAiExplanationTest {

    @Test
    void generatesValidGroundedAiExplanation() {
        String explanationId = UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        GroundedAiExplanation aiExplanation = GroundedAiExplanation.generate(
            explanationId, 
            traceId, 
            "The authentication failed because your SPF record is missing.", 
            now
        );
        
        assertNotNull(aiExplanation.id());
        assertEquals(explanationId, aiExplanation.sourceExplanationId());
        assertEquals(traceId, aiExplanation.groundingEvidenceTraceId());
        assertEquals("The authentication failed because your SPF record is missing.", aiExplanation.aiGeneratedContent());
        assertEquals(now, aiExplanation.generatedAt());
    }

    @Test
    void rejectsEmptyContentOrTrace() {
        assertThrows(IllegalArgumentException.class, () -> 
            GroundedAiExplanation.generate("exp-1", "trace-1", "   ", Instant.now())
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            GroundedAiExplanation.generate("exp-1", "   ", "Content", Instant.now())
        );
    }

    @Test
    void rejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> 
            GroundedAiExplanation.generate(null, "trace-1", "Content", Instant.now())
        );
    }
}
