package com.mailsent.identity.authentication.interfaces;

/**
 * SignUpResponse — the outbound sign-up contract. Confirms the identity and role that
 * were created. Never echoes the password (raw or hashed). The home Organization id stays
 * internal to the backend (ORG-002 decision); it is not part of this public contract.</p>
 *
 * @param id    the stable identity of the new user
 * @param email the canonical email
 * @param role  the assigned role (business label)
 */
public record SignUpResponse(String id, String email, String role) {
}