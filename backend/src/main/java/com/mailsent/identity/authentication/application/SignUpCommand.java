package com.mailsent.identity.authentication.application;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.PasswordPolicy;

/**
 * SignUpCommand — the inputs to the sign-up use case, carried from the interfaces
 * layer to the application layer.
 *
 * <p>Holds the raw email and raw password exactly as offered. Raw values are validated
 * by the domain ({@link Email}, {@link PasswordPolicy}) inside the use case; a raw
 * password is never stored or logged.</p>
 *
 * @param email    the offered email address
 * @param password the offered password
 */
public record SignUpCommand(String email, String password) {
}