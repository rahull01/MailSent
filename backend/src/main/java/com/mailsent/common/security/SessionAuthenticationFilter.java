package com.mailsent.common.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.common.error.ApiError;
import com.mailsent.identity.authentication.application.SessionRepository;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.Session;
import com.mailsent.identity.authentication.domain.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SessionAuthenticationFilter — the authoritative HTTP authentication filter (REM-SEC-001).
 *
 * <p>Enforces bearer token validation against durable session storage. Computes the
 * SHA-256 hash of inbound bearer tokens, verifies active expiry bounds, loads user
 * identity, and establishes the request's {@link TenantContext} and SLF4J MDC context.</p>
 *
 * <p>Guaranteed cleanups in a {@code finally} block prevent cross-request tenant bleeding
 * across pooled Servlet threads (INV-09, NFR-S1).</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public SessionAuthenticationFilter(SessionRepository sessionRepository,
                                       UserRepository userRepository,
                                       Optional<Clock> clock,
                                       ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.clock = clock.orElseGet(Clock::systemUTC);
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Allow HTTP OPTIONS (CORS preflight) without bearer token checks
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = normalizePath(request.getRequestURI());
        String method = request.getMethod();

        // 2. Allow unauthenticated public endpoints
        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract Authorization header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || authHeader.isBlank()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED",
                "Authentication is required to access this resource.");
            return;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_FORMAT",
                "Authorization header must use Bearer scheme.");
            return;
        }

        String rawToken = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (rawToken.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_FORMAT",
                "Authorization header must use Bearer scheme.");
            return;
        }

        // 4. Hash raw token and lookup session
        String tokenHash = hashToken(rawToken);
        Optional<Session> sessionOpt = sessionRepository.findByTokenHash(tokenHash);
        if (sessionOpt.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN",
                "Session is invalid or does not exist.");
            return;
        }

        Session session = sessionOpt.get();

        // 5. Verify session expiry
        Instant now = clock.instant();
        if (!session.isActive(now)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED",
                "Your session has expired. Please sign in again.");
            return;
        }

        // 6. Verify user account exists
        Optional<User> userOpt = userRepository.findById(session.userId());
        if (userOpt.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND",
                "Authenticated user account no longer exists.");
            return;
        }

        User user = userOpt.get();

        // 7. Bind AuthenticatedIdentity and SLF4J MDC
        AuthenticatedIdentity identity = new AuthenticatedIdentity(
            user.id(), user.email().value(), user.organizationId(), user.role());

        TenantContext.set(identity);
        MDC.put("organizationId", identity.organizationId());
        MDC.put("userId", identity.userId());

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.remove("organizationId");
            MDC.remove("userId");
        }
    }

    /**
     * Trailing slashes are normalized before matching so proxies/clients that keep a
     * trailing slash still hit the public-endpoint allowlist (POST /api/sessions/ must
     * behave identically to POST /api/sessions).
     */
    private static String normalizePath(String uri) {
        if (uri == null) {
            return "";
        }
        int end = uri.length();
        while (end > 1 && uri.charAt(end - 1) == '/') {
            end--;
        }
        return end == uri.length() ? uri : uri.substring(0, end);
    }

    private boolean isPublicEndpoint(String path, String method) {
        if ("/api/sessions".equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if ("/api/identities".equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if ("/api/invitations/accept".equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.startsWith("/ingest/dmarc/rua/")) {
            return true;
        }
        if (path.startsWith("/actuator")) {
            return true;
        }
        return false;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // If rawToken is hex-encoded 32-byte string (from SecureSessionTokenFactory), decode to raw bytes
            byte[] bytes;
            if (rawToken.length() == 64 && isHex(rawToken)) {
                bytes = HexFormat.of().parseHex(rawToken);
            } else {
                bytes = rawToken.getBytes(StandardCharsets.UTF_8);
            }
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = ApiError.simple(status.name(), code, message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
