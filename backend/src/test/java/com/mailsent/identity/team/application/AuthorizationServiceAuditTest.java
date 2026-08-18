package com.mailsent.identity.team.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessAuditRecord;
import com.mailsent.identity.team.domain.AccessAuditSink;
import com.mailsent.identity.team.domain.AccessDecision;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.domain.Capability;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorizationService Audit Tests (TEAM-003)")
class AuthorizationServiceAuditTest {

    private List<AccessAuditRecord> recordedAudits;
    private AccessAuditSink auditSink;
    private Clock fixedClock;
    private AuthorizationService authorizationService;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        recordedAudits = new ArrayList<>();
        auditSink = recordedAudits::add;
        fixedInstant = Instant.parse("2026-08-18T10:00:00Z");
        fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
        authorizationService = new AuthorizationService(auditSink, fixedClock);
    }

    private User createUser(String id, String email, Role role, String orgId) {
        return User.restore(id, new Email(email), "$2a$12$dummyhash", role, fixedInstant, orgId);
    }

    @Test
    @DisplayName("Authorized operation emits GRANTED audit record")
    void emitsGrantedAuditRecord() {
        User owner = createUser("usr-owner", "owner@a.com", Role.OWNER, "org-a");

        boolean result = authorizationService.isAuthorized(owner, "org-a", Capability.HEALTH_VIEW);

        assertTrue(result);
        assertEquals(1, recordedAudits.size());
        AccessAuditRecord audit = recordedAudits.get(0);
        assertEquals(fixedInstant, audit.occurredAt());
        assertEquals("usr-owner", audit.userId());
        assertEquals("org-a", audit.userOrganizationId());
        assertEquals("org-a", audit.targetOrganizationId());
        assertEquals(Capability.HEALTH_VIEW, audit.capability());
        assertEquals(AccessDecision.GRANTED, audit.decision());
        assertEquals("Authorized", audit.reason());
    }

    @Test
    @DisplayName("Unauthorized role capability emits DENIED audit record")
    void emitsDeniedAuditRecordOnUnauthorizedRole() {
        User viewer = createUser("usr-viewer", "viewer@a.com", Role.VIEWER, "org-a");

        boolean result = authorizationService.isAuthorized(viewer, "org-a", Capability.DOMAIN_OBSERVE_TRIGGER);

        assertFalse(result);
        assertEquals(1, recordedAudits.size());
        AccessAuditRecord audit = recordedAudits.get(0);
        assertEquals(fixedInstant, audit.occurredAt());
        assertEquals("usr-viewer", audit.userId());
        assertEquals("org-a", audit.userOrganizationId());
        assertEquals("org-a", audit.targetOrganizationId());
        assertEquals(Capability.DOMAIN_OBSERVE_TRIGGER, audit.capability());
        assertEquals(AccessDecision.DENIED, audit.decision());
        assertTrue(audit.reason().contains("Role VIEWER not granted capability DOMAIN_OBSERVE_TRIGGER"));
    }

    @Test
    @DisplayName("Multi-tenant violation (INV-09) emits DENIED audit record with boundary mismatch details")
    void emitsDeniedAuditRecordOnTenantMismatch() {
        User ownerOrgA = createUser("usr-owner-a", "owner@a.com", Role.OWNER, "org-a");

        boolean result = authorizationService.isAuthorized(ownerOrgA, "org-b", Capability.HEALTH_VIEW);

        assertFalse(result);
        assertEquals(1, recordedAudits.size());
        AccessAuditRecord audit = recordedAudits.get(0);
        assertEquals(fixedInstant, audit.occurredAt());
        assertEquals("usr-owner-a", audit.userId());
        assertEquals("org-a", audit.userOrganizationId());
        assertEquals("org-b", audit.targetOrganizationId());
        assertEquals(Capability.HEALTH_VIEW, audit.capability());
        assertEquals(AccessDecision.DENIED, audit.decision());
        assertTrue(audit.reason().contains("Multi-tenant violation"));
    }

    @Test
    @DisplayName("Null actor / parameters emit DENIED audit records cleanly without throwing runtime exceptions")
    void emitsDeniedOnNullInputs() {
        assertFalse(authorizationService.isAuthorized(null, "org-a", Capability.HEALTH_VIEW));
        assertEquals(1, recordedAudits.size());
        assertEquals(AccessDecision.DENIED, recordedAudits.get(0).decision());
        assertEquals("anonymous", recordedAudits.get(0).userId());

        recordedAudits.clear();
        User owner = createUser("u1", "u1@a.com", Role.OWNER, "org-a");
        assertFalse(authorizationService.isAuthorized(owner, null, Capability.HEALTH_VIEW));
        assertEquals(1, recordedAudits.size());
        assertEquals(AccessDecision.DENIED, recordedAudits.get(0).decision());

        recordedAudits.clear();
        assertFalse(authorizationService.isAuthorized(owner, "org-a", null));
        assertEquals(1, recordedAudits.size());
        assertEquals(AccessDecision.DENIED, recordedAudits.get(0).decision());
    }
}
