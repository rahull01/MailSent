package com.mailsent.identity.authentication.interfaces;

/**
 * SignUpResponse — the outbound sign-up contract. Confirms the identity and role that
 * were created. Never echoes the password (raw or hashed).</p>
 *
 * @param id    the stable identity of the new user
 * @param email the canonical email
 * @param role  the assigned role (business label)
 */
public record SignUpResponse(String id, String email, String role) {
}