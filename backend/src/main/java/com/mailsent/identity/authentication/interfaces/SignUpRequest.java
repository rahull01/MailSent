package com.mailsent.identity.authentication.interfaces;

import jakarta.validation.constraints.NotBlank;

/**
 * SignUpRequest — the inbound sign-up contract (API philosophy: explicit semantics,
 * no implementation leakage). Carries the offered email and password.
 *
 * <p>Bean-validation footguns are kept to presence/blank checks here; the meaning is
 * validated in the domain (Email format + PasswordPolicy). Error language is
 * business-first and never technical.</p>
 *
 * @param email    the offered email address
 * @param password the offered password
 */
public record SignUpRequest(
    @NotBlank(message = "An email address is required.") String email,
    @NotBlank(message = "A password is required.") String password
) {
}