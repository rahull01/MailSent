package com.mailsent.identity.team.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mailsent.identity.authentication.application.SignUpCommand;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.team.application.InvitationIssued;
import com.mailsent.identity.team.application.InvitationService;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * Integration tests for the TEAM-001 acceptance endpoint ({@code POST /api/invitations/accept}).
 *
 * <p>Runs against the real Spring context in the hermetic profile (in-memory repositories),
 * exercising the full interface → application → domain path. Seeds a target organization
 * and an existing account, invites it, and drives the endpoint through success and the
 * documented failure shapes — business-first ApiErrors, no implementation leakage.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvitationAcceptControllerIntegrationTest {

    private static final String ENDPOINT = "/api/invitations/accept";
    private static final String PWD = "a-sufficiently-long-password-1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InvitationService invitationService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private SignUpService signUpService;

    private String createTargetOrganization() {
        Organization org = organizationService.create("Target Acme");
        return org.id();
    }

    private void createInviteeAccount(String email) {
        // An existing account that will accept the invitation (decision 8). The shared
        // in-memory UserRepository persists across test methods, so each test uses a
        // unique email to keep cases isolated.
        signUpService.signUp(new SignUpCommand(email, PWD));
    }

    private String uniqueEmail(String prefix) {
        return "it-" + java.util.UUID.randomUUID() + "-" + prefix + "@example.com";
    }

    @Test
    void acceptReturnsEstablishedMembership() throws Exception {
        String email = uniqueEmail("accept");
        createInviteeAccount(email);
        String orgId = createTargetOrganization();
        InvitationIssued issued = invitationService.invite(orgId, email, "VIEWER");

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + issued.rawToken() + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.membershipId").exists())
            .andExpect(jsonPath("$.organizationId").value(orgId))
            .andExpect(jsonPath("$.role").value("Viewer"));
    }

    @Test
    void acceptingTheSameInvitationTwiceIsRejected() throws Exception {
        String email = uniqueEmail("twice");
        createInviteeAccount(email);
        String orgId = createTargetOrganization();
        InvitationIssued issued = invitationService.invite(orgId, email, "VIEWER");
        String body = "{\"token\":\"" + issued.rawToken() + "\"}";

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVITATION_ALREADY_ACCEPTED"));
    }

    @Test
    void acceptingAnUnknownTokenIsNotFound() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"deadbeefdeadbeef\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INVALID_INVITATION_TOKEN"));
    }
}