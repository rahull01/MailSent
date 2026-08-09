package com.mailsent;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testing foundation baseline.
 *
 * Verifies the Spring context loads and that the cross-cutting error handler
 * returns a business-safe, structured error (no leakage) for an unknown
 * resource.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MailSentApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Spring context is created and wires the foundation beans.
    }

    @Test
    void unknownResourceReturnsStructuredError() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("The requested resource was not found.")));
    }
}