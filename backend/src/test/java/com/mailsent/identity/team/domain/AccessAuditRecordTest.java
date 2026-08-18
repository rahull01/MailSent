package com.mailsent.identity.team.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccessAuditRecord Value Object Tests (TEAM-003)")
class AccessAuditRecordTest {

    @Test
    @DisplayName("Creates immutable granted audit record with complete fields")
    void createsGrantedRecord() {
        Instant now = Instant.now();
        AccessAuditRecord record = AccessAuditRecord.granted(now, "usr-1", "org-1", "org-1", Capability.HEALTH_VIEW);

        assertEquals(now, record.occurredAt());
        assertEquals("usr-1", record.userId());
        assertEquals("org-1", record.userOrganizationId());
        assertEquals("org-1", record.targetOrganizationId());
        assertEquals(Capability.HEALTH_VIEW, record.capability());
        assertEquals(AccessDecision.GRANTED, record.decision());
        assertEquals("Authorized", record.reason());
    }

    @Test
    @DisplayName("Creates immutable denied audit record with reason")
    void createsDeniedRecord() {
        Instant now = Instant.now();
        AccessAuditRecord record = AccessAuditRecord.denied(now, "usr-2", "org-a", "org-b", Capability.DOMAIN_MANAGE, "Tenant boundary mismatch");

        assertEquals(now, record.occurredAt());
        assertEquals("usr-2", record.userId());
        assertEquals("org-a", record.userOrganizationId());
        assertEquals("org-b", record.targetOrganizationId());
        assertEquals(Capability.DOMAIN_MANAGE, record.capability());
        assertEquals(AccessDecision.DENIED, record.decision());
        assertEquals("Tenant boundary mismatch", record.reason());
    }

    @Test
    @DisplayName("Null fields default to safe placeholders")
    void nullFieldsDefaultToPlaceholders() {
        Instant now = Instant.now();
        AccessAuditRecord record = new AccessAuditRecord(now, null, null, null, null, AccessDecision.DENIED, null);

        assertEquals("anonymous", record.userId());
        assertEquals("none", record.userOrganizationId());
        assertEquals("none", record.targetOrganizationId());
        assertEquals("", record.reason());
        assertNull(record.capability());
    }
}
