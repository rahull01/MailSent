package com.mailsent.identity.authentication.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Email — the canonical identity of a User (Glossary: User; Ubiquitous Language:
 * the business noun). An immutable value object that is always valid.
 *
 * <p>Enforces a single, stable email format and a bounded length so that invalid or
 * over-long input is rejected at the boundary before it can reach storage or a
 * password. Validation satisfies NFR-S4 (validate all inputs, never trust by default).</p>
 */
public record Email(String value) {

    /**
     * Practical email shape. Deliberately conservative and implementation-free:
     * local@domain, a single {@code @}, sensible label boundaries, and no
     * whitespace or control characters (blocks header/HTML/shell injection pathways).
     */
    private static final Pattern PATTERN = Pattern.compile(
        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$"
    );

    /** Upper bound from common practice; trivially avoids abuse. */
    private static final int MAX_LENGTH = 254;

    /**
     * Creates an Email, validating format and length.
     *
     * @param value the raw address, case-insensitive
     * @throws InvalidEmailException if {@code value} is null, blank, too long, or malformed
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("An email address is required.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidEmailException("The email address is too long.");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("The email address is not valid.");
        }
    }
}