package com.mailsent.identity.authentication.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mailsent.identity.authentication.infrastructure.InMemorySessionRepository;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;

/**
 * Integration tests for the sign-in endpoint (AUTH-001), exercised end-to-end against
 * the real Spring context (hermetic profile — in-memory persistence). The sign-in flow
 * relies on a registered identity, so it seeds one via the sign-up endpoint first,
 * which also re-verifies AUTH-000 (regression) as a dependency of the flow.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignInControllerIntegrationTest {

    private static final String SIGN_UP = "/api/identities";
    private static final String SIGN_IN = "/api/sessions";
    private static final String PWD = "a-sufficiently-long-password-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryUserRepository users;

    @Autowired
    private InMemorySessionRepository sessions;

    @BeforeEach
    void registerAnAccount() throws Exception {
        // Reset shared hermetic state so each case starts clean (uniqueness is guarded
        // by the in-memory repo, matching the production unique constraint).
        users.clear();
        sessions.clear();
        mockMvc.perform(post(SIGN_UP)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@example.com\",\"password\":\"" + PWD + "\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void validSignInReturnsSessionWithOneTimeToken() throws Exception {
        mockMvc.perform(post(SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@example.com\",\"password\":\"" + PWD + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("login@example.com"))
            .andExpect(jsonPath("$.role").value("Viewer"))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.expiresAtEpochSecond").exists());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mockMvc.perform(post(SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@example.com\",\"password\":\"wrong-password-entirely\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void unknownEmailReturnsIdentical401() throws Exception {
        mockMvc.perform(post(SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"" + PWD + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("The email or password is incorrect."));
    }

    @Test
    void missingFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post(SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unreadableBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json"))
            .andExpect(status().isBadRequest());
    }
}