package com.mailsent.common.error;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mailsent.common.security.UnauthenticatedException;
import com.mailsent.identity.consent.application.ConsentAlreadyExistsException;
import com.mailsent.identity.consent.application.ConsentRequiredException;
import com.mailsent.identity.team.domain.AccessDeniedException;

/**
 * Cross-cutting error handling foundation.
 *
 * Converts exceptions into a business-first, implementation-free
 * {@link ApiError}. Never returns stack traces or internals to the client.
 * Bean Validation failures become structured field errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> new ApiError.FieldError(f.getField(), f.getDefaultMessage()))
            .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(
                new ApiError("BAD_REQUEST", "VALIDATION_FAILED",
                        "Some fields could not be accepted.", fields, java.time.Instant.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> onUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiError.simple(
                "BAD_REQUEST", "MALFORMED", "The request could not be read."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> onNotFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.simple("NOT_FOUND", "NOT_FOUND", "The requested resource was not found."));
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ApiError> onUnauthenticated(UnauthenticatedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.simple("UNAUTHORIZED", "AUTH_REQUIRED", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> onSecurity(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.simple("FORBIDDEN", "INSUFFICIENT_PERMISSIONS", ex.getMessage()));
    }

    @ExceptionHandler(ConsentAlreadyExistsException.class)
    public ResponseEntity<ApiError> onConsentAlreadyExists(ConsentAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.simple("CONFLICT", "CONSENT_ALREADY_EXISTS", ex.getMessage()));
    }

    /** A missing/revoked consent is a policy refusal (INV-02), never a server error. */
    @ExceptionHandler(ConsentRequiredException.class)
    public ResponseEntity<ApiError> onConsentRequired(ConsentRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.simple("FORBIDDEN", "CONSENT_REQUIRED", ex.getMessage()));
    }

    /**
     * Tenant-boundary and capability denials across the platform surface as 403 —
     * authorization refusals must never render as 500s.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> onAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.simple("FORBIDDEN", "INSUFFICIENT_PERMISSIONS", ex.getMessage()));
    }

    /** Domain-level input rejections that reach the web layer map to a client error, not 500. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> onIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.simple("BAD_REQUEST", "INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        // Business-safe message; never leak the internal cause.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.simple("INTERNAL_ERROR", "INTERNAL",
                        "Something went wrong. We have been told and logged it; we never guess an answer."));
    }
}