package com.mailsent.identity.authentication.interfaces;

/**
 * SignInResponse — the outbound sign-in contract.
 *
 * <p>Confirms the created session and authenticated identity. Carries the <strong>raw
 * session token exactly once</strong> — this is the value the caller presents on
 * subsequent requests. The raw token is never persisted server-side (only its hash).
 * The password is never echoed.</p>
 *
 * @param sessionId the stable identity of the created session
 * @param userId    the authenticated user's identity
 * @param email     the canonical email
 * @param role      the authenticated user's role
 * @param token     the one-time raw session token
 * @param expiresAtEpochSecond when the session expires (epoch seconds)
 */
public record SignInResponse(
    String sessionId,
    String userId,
    String email,
    String role,
    String token,
    long expiresAtEpochSecond
) {
}