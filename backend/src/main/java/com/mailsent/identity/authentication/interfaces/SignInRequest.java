package com.mailsent.identity.authentication.interfaces;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * SignInRequest — the inbound sign-in contract (API philosophy: explicit semantics, no
 * implementation leakage). Carries the offered email and password.
 *
 * <p>Presence/blank checks here; {@code @Email} enforces a well-formed address. The
 * password is never stored or logged; the use case only verifies it.</p>
 *
 * @param email    the offered email address
 * @param password the offered password
 */
public record SignInRequest(
    @NotBlank(message = "An email address is required.")
    @Email(message = "The email address is not valid.") String email,
    @NotBlank(message = "A password is required.") String password
) {
}