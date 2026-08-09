package com.mailsent.identity.authentication.application;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.SessionToken;

/**
 * SignInResult — the outcome of a successful sign-in.
 *
 * <p>Carries the raw session token (returned to the caller exactly once — never
 * persisted) and the authenticated identity and role. The raw token is what the caller
 * presents on subsequent requests to prove the session.</p>
 *
 * @param sessionId  the stable identity of the created session
 * @param userId     the authenticated user's identity
 * @param email      the canonical email of the authenticated user
 * @param role       the authenticated user's role
 * @param token      the raw session token (shown once, stored only as a hash)
 * @param expiresAtEpochSecond the instant the session expires, as epoch seconds
 */
public record SignInResult(
    String sessionId,
    String userId,
    String email,
    Role role,
    SessionToken token,
    long expiresAtEpochSecond
) {
}