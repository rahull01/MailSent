package com.mailsent.common.security;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.Session;
import com.mailsent.identity.authentication.domain.SessionToken;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.InMemorySessionRepository;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.authentication.infrastructure.SecureSessionTokenFactory;

import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.*;

class SessionAuthenticationFilterTest {

    private InMemorySessionRepository sessionRepository;
    private InMemoryUserRepository userRepository;
    private SecureSessionTokenFactory tokenFactory;
    private ObjectMapper objectMapper;
    private Clock fixedClock;
    private Instant now;
    private SessionAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        sessionRepository = new InMemorySessionRepository();
        userRepository = new InMemoryUserRepository();
        tokenFactory = new SecureSessionTokenFactory();
        objectMapper = new ObjectMapper().findAndRegisterModules();
        now = Instant.parse("2026-08-20T12:00:00Z");
        fixedClock = Clock.fixed(now, ZoneOffset.UTC);
        filter = new SessionAuthenticationFilter(
            sessionRepository,
            userRepository,
            Optional.of(fixedClock),
            objectMapper
        );
        TenantContext.clear();
        MDC.clear();
    }

    @Test
    void publicEndpoint_AllowsUnauthenticatedRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sessions");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertNotNull(filterChain.getRequest());
        assertTrue(TenantContext.get().isEmpty());
    }

    @Test
    void protectedEndpoint_MissingAuthorizationHeader_Returns401AuthRequired() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("AUTH_REQUIRED"));
        assertNull(filterChain.getRequest());
    }

    @Test
    void protectedEndpoint_MalformedHeader_Returns401InvalidTokenFormat() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("INVALID_TOKEN_FORMAT"));
    }

    @Test
    void protectedEndpoint_InvalidTokenHash_Returns401InvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        request.addHeader("Authorization", "Bearer 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("INVALID_TOKEN"));
    }

    @Test
    void protectedEndpoint_ExpiredSession_Returns401SessionExpired() throws ServletException, IOException {
        User user = User.register(new Email("user@org.com"), "hashed_pw", now.minusSeconds(3600), "org-1");
        userRepository.save(user);

        SessionToken token = tokenFactory.randomToken();
        // Issued 2 hours ago with 1 hour lifetime -> expired
        Session expiredSession = Session.issue(user.id(), token, now.minus(Duration.ofHours(2)), Duration.ofHours(1));
        sessionRepository.save(expiredSession);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        request.addHeader("Authorization", "Bearer " + token.rawToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("SESSION_EXPIRED"));
    }

    @Test
    void protectedEndpoint_DeletedUser_Returns401UserNotFound() throws ServletException, IOException {
        SessionToken token = tokenFactory.randomToken();
        Session session = Session.issue("non-existent-user", token, now, Duration.ofHours(24));
        sessionRepository.save(session);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        request.addHeader("Authorization", "Bearer " + token.rawToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("USER_NOT_FOUND"));
    }

    @Test
    void protectedEndpoint_ValidBearerToken_PopulatesTenantContextAndMdc() throws ServletException, IOException {
        User user = User.register(new Email("analyst@org1.com"), "hashed_pw", now, "org-1");
        userRepository.save(user);

        SessionToken token = tokenFactory.randomToken();
        Session session = Session.issue(user.id(), token, now, Duration.ofHours(24));
        sessionRepository.save(session);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        request.addHeader("Authorization", "Bearer " + token.rawToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<AuthenticatedIdentity> capturedContext = new AtomicReference<>();
        AtomicReference<String> capturedMdcOrg = new AtomicReference<>();
        AtomicReference<String> capturedMdcUser = new AtomicReference<>();

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                capturedContext.set(TenantContext.get().orElse(null));
                capturedMdcOrg.set(MDC.get("organizationId"));
                capturedMdcUser.set(MDC.get("userId"));
            }
        };

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertNotNull(capturedContext.get());
        assertEquals(user.id(), capturedContext.get().userId());
        assertEquals("org-1", capturedContext.get().organizationId());
        assertEquals(Role.VIEWER, capturedContext.get().role());
        assertEquals("org-1", capturedMdcOrg.get());
        assertEquals(user.id(), capturedMdcUser.get());

        // Verify cleanup after filter finishes
        assertTrue(TenantContext.get().isEmpty());
        assertNull(MDC.get("organizationId"));
        assertNull(MDC.get("userId"));
    }

    @Test
    void filter_GuaranteesTenantContextAndMdcCleanup_InFinallyBlock() {
        User user = User.register(new Email("admin@org1.com"), "hashed_pw", now, "org-1");
        userRepository.save(user);

        SessionToken token = tokenFactory.randomToken();
        Session session = Session.issue(user.id(), token, now, Duration.ofHours(24));
        sessionRepository.save(session);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/consents");
        request.addHeader("Authorization", "Bearer " + token.rawToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain failingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                throw new RuntimeException("Downstream error");
            }
        };

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, failingChain));

        // Guaranteed cleanup verified
        assertTrue(TenantContext.get().isEmpty());
        assertNull(MDC.get("organizationId"));
        assertNull(MDC.get("userId"));
    }
}
