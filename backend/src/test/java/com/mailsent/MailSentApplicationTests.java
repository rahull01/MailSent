package com.mailsent;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mailsent.identity.authentication.application.SessionRepository;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Session;
import com.mailsent.identity.authentication.domain.SessionToken;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.SecureSessionTokenFactory;
import com.mailsent.portfolio.observation.application.ReObservationScheduler;
import com.mailsent.portfolio.observation.application.ReObservationService;

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

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SecureSessionTokenFactory tokenFactory;

    @Test
    void contextLoads() {
        // Spring context is created and wires the foundation beans.
    }

    @Test
    void testProfileDoesNotRegisterSchedulerOrScheduledProcessing() {
        assertEquals(0, context.getBeanNamesForType(ReObservationScheduler.class).length);
        assertEquals(0, context.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class).length);
        assertTrue(context.getBeanNamesForType(ReObservationService.class).length > 0);
    }

    @Test
    void unknownResourceReturnsStructuredError() throws Exception {
        Instant now = Instant.now();
        User user = User.register(new Email("smoke@example.com"), "hashed_pw", now, "org-smoke");
        userRepository.save(user);

        SessionToken token = tokenFactory.randomToken();
        Session session = Session.issue(user.id(), token, now, Duration.ofHours(24));
        sessionRepository.save(session);

        mockMvc.perform(get("/api/does-not-exist")
                .header("Authorization", "Bearer " + token.rawToken()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("The requested resource was not found.")));
    }
}