package com.mailsent.identity.authentication.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.mailsent.identity.authentication.application.SignUpCommand;
import com.mailsent.identity.authentication.application.SignUpResult;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.InvalidEmailException;
import com.mailsent.identity.authentication.domain.WeakPasswordException;

import com.mailsent.common.error.ApiError;

/**
 * SignUpController — the sign-up business contract (AUTH-000).
 *
 * <p>Receives an offered email and password, runs the {@link SignUpService} use case,
 * and returns the created identity and role. Errors are mapped to business-first
 * {@link ApiError}s with no implementation leakage, following the API error philosophy
 * and the Constitution (no vague failures, never reveal internals).</p>
 *
 * <p>A successful sign-up does not start a session; session management is AUTH-001.</p>
 */
@RestController
@RequestMapping("/api/identities")
public class SignUpController {

    private final SignUpService signUpService;

    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    /** Registers a new identity, returning the assigned role. */
    @PostMapping
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResult result = signUpService.signUp(
            new SignUpCommand(request.email(), request.password()));
        SignUpResponse response = new SignUpResponse(result.id(), result.email(), result.role().label());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> onDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "EMAIL_IN_USE", ex.getMessage()));
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ApiError> onInvalidEmail(InvalidEmailException ex) {
        return ResponseEntity.badRequest()
            .body(ApiError.simple("BAD_REQUEST", "INVALID_EMAIL", ex.getMessage()));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ApiError> onWeakPassword(WeakPasswordException ex) {
        return ResponseEntity.badRequest()
            .body(ApiError.simple("BAD_REQUEST", "WEAK_PASSWORD", ex.getMessage()));
    }
}