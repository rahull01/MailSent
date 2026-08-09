package com.mailsent.common.error;

import java.time.Instant;
import java.util.List;

/**
 * Structured API error returned by the {@link GlobalExceptionHandler}.
 *
 * Uses business-first language and never leaks implementation details.
 * Foundation for the error-handling requirement (no opaque errors, no
 * implementation leakage in the error body).
 */
public record ApiError(
    String status,
    String code,
    String message,
    List<FieldError> fieldErrors,
    Instant timestamp
) {

    /** A single field validation failure. */
    public record FieldError(String field, String message) {}

    /** Build a simple error without field-level details. */
    public static ApiError simple(String status, String code, String message) {
        return new ApiError(status, code, message, List.of(), Instant.now());
    }
}