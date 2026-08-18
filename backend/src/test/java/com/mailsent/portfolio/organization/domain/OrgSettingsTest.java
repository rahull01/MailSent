package com.mailsent.portfolio.organization.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class OrgSettingsTest {

    @Test
    void managesAlertDigestConfig() {
        OrgAlertDigestConfig config = OrgAlertDigestConfig.defaultConfig("org-1", "team@agency.com");
        assertEquals(OrgAlertDigestConfig.DigestCadence.DAILY, config.cadence());
        assertEquals("team@agency.com", config.digestRecipientEmail());

        config.update(OrgAlertDigestConfig.DigestCadence.WEEKLY, "exec@agency.com", true);
        assertEquals(OrgAlertDigestConfig.DigestCadence.WEEKLY, config.cadence());
        assertEquals("exec@agency.com", config.digestRecipientEmail());
    }

    @Test
    void managesTeamRoles() {
        Instant now = Instant.now();
        OrgTeamMember member = OrgTeamMember.addMember("org-1", "usr-1", "dev@agency.com", OrgTeamMember.OrgRole.OPERATOR, now);
        assertEquals(OrgTeamMember.OrgRole.OPERATOR, member.role());

        member.updateRole(OrgTeamMember.OrgRole.ADMIN);
        assertEquals(OrgTeamMember.OrgRole.ADMIN, member.role());
    }

    @Test
    void controlsDomainLifecycle() {
        Instant now = Instant.now();
        DomainManagementControl control = DomainManagementControl.init("org-1", "domain-1", now);
        assertEquals(DomainManagementControl.MonitoringState.ACTIVE, control.state());

        control.pause("Client seasonal freeze", now.plusSeconds(60));
        assertEquals(DomainManagementControl.MonitoringState.PAUSED, control.state());
        assertEquals("Client seasonal freeze", control.reason());

        control.resume(now.plusSeconds(120));
        assertEquals(DomainManagementControl.MonitoringState.ACTIVE, control.state());

        control.remove("Domain decommissioned", now.plusSeconds(180));
        assertEquals(DomainManagementControl.MonitoringState.REMOVED, control.state());
    }
}
