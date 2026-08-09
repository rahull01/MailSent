package com.mailsent.identity.authentication.application;

import com.mailsent.identity.authentication.domain.Role;

/**
 * SignUpResult — the outcome of a successful sign-up, exposed to callers.
 *
 * <p>Never carries the password, hashed or raw. Returns only the stable identity, the
 * canonical email, and the role assigned at creation, per least-authority disclosure.</p>
 *
 * @param id    the stable identity of the new user
 * @param email the canonical email
 * @param role  the assigned role
 */
public record SignUpResult(String id, String email, Role role) {
}