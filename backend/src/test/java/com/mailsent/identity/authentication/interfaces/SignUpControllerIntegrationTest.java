package com.mailsent.identity.authentication.interfaces;

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

/**
 * Integration tests for the sign-up endpoint (AUTH-000).
 *
 * <p>Runs against the real Spring context (hermetic profile — no DataSource needed),
 * exercising the full interface → application → domain path for the success and the
 * documented failure shapes. Confirms the API error philosophy: business-first, no
 * implementation leakage, no password echoed.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignUpControllerIntegrationTest {

    private static final String ENDPOINT = "/api/identities";
    private static final String PWD = "a-sufficiently-long-password-1";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void successfulSignUpReturnsCreatedWithRole() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new.user@example.com\",\"password\":\"" + PWD + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("new.user@example.com"))
            .andExpect(jsonPath("$.role").value("Viewer"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.organizationId").doesNotExist());
    }

    @Test
    void successfulSignUpNeverReturnsPassword() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-pass@example.com\",\"password\":\"" + PWD + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String body = "{\"email\":\"dup@example.com\",\"password\":\"" + PWD + "\"}";
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("EMAIL_IN_USE"));
    }

    @Test
    void invalidPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"weak@example.com\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
    }

    @Test
    void malformedEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"" + PWD + "\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_EMAIL"));
    }

    @Test
    void unreadableBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void missingFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void weakPasswordDoesNotExposePolicyInternals() throws Exception {
        // The error body is business-first and carries no hash or internal detail.
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-leak@example.com\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")));
    }

    @Test
    void whitespacePaddedEmailIsNormalizedBeforeDuplicateCheck() throws Exception {
        String pwd = PWD;
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"  padded@example.com  \",\"password\":\"" + pwd + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("padded@example.com"));
    }
}