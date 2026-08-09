package com.mailsent.identity.authentication.application;

/**
 * SignInCommand — the inputs to the sign-in use case, carried from the interfaces
 * layer to the application layer.
 *
 * <p>Holds the offered email and password exactly as submitted. The raw password is
 * validated and consumed inside the use case; it is never stored or logged.</p>
 *
 * @param email    the offered email address
 * @param password the offered password
 */
public record SignInCommand(String email, String password) {
}