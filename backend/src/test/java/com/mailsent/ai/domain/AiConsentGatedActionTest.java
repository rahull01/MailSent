package com.mailsent.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AiConsentGatedActionTest {

    @Test
    void proposesValidAction() {
        String recommendationId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        AiConsentGatedAction action = AiConsentGatedAction.propose(
            recommendationId, 
            "{ \"type\": \"UPDATE_DNS\", \"record\": \"TXT\" }", 
            now
        );
        
        assertNotNull(action.id());
        assertEquals(recommendationId, action.targetRecommendationId());
        assertEquals("{ \"type\": \"UPDATE_DNS\", \"record\": \"TXT\" }", action.proposedActionPayload());
        assertEquals(AiConsentGatedAction.ActionStatus.PROPOSED, action.status());
        assertEquals(now, action.statusUpdatedAt());
    }

    @Test
    void grantsConsentValidly() {
        AiConsentGatedAction action = AiConsentGatedAction.propose(
            "rec-1", "payload", Instant.now()
        );
        
        Instant grantedAt = Instant.now().plusSeconds(10);
        action.grantConsent(grantedAt);
        
        assertEquals(AiConsentGatedAction.ActionStatus.CONSENT_GRANTED, action.status());
        assertEquals(grantedAt, action.statusUpdatedAt());
    }

    @Test
    void rejectsDoubleConsent() {
        AiConsentGatedAction action = AiConsentGatedAction.propose(
            "rec-1", "payload", Instant.now()
        );
        
        action.grantConsent(Instant.now());
        
        assertThrows(IllegalStateException.class, () -> 
            action.grantConsent(Instant.now())
        );
    }
}
