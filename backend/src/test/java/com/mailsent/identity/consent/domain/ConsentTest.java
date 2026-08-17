package com.mailsent.identity.consent.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ConsentTest {

    @Test
    void grantConsent() {
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        assertNotNull(consent.getId());
        assertEquals("org-1", consent.getOrganizationId());
        assertEquals("domain-1", consent.getDomainId());
        assertEquals("user-1", consent.getGrantedByUserId());
        assertEquals(ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE, consent.getActionType());
        assertEquals(Status.GRANTED, consent.getStatus());
        assertNotNull(consent.getGrantedAt());
        assertNull(consent.getRevokedAt());
    }

    @Test
    void revokeConsent() {
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        Instant revokeTime = Instant.now();
        consent.revoke("user-2", revokeTime);

        assertEquals(Status.REVOKED, consent.getStatus());
        assertEquals(revokeTime, consent.getRevokedAt());
    }
}
