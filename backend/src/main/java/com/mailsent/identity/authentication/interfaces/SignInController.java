package com.mailsent.identity.authentication.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.mailsent.identity.authentication.application.SignInCommand;
import com.mailsent.identity.authentication.application.SignInResult;
import com.mailsent.identity.authentication.application.SignInService;
import com.mailsent.identity.authentication.domain.InvalidCredentialsException;

import com.mailsent.common.error.ApiError;

/**
 * SignInController — the sign-in business contract (AUTH-001).
 *
 * <p>Accepts an offered email and password, runs {@link SignInService}, and returns the
 * created session with its one-time raw token. Failed credentials always yield the same
 * {@code 401} with a unified message — never distinguishing an unknown email from a wrong
 * password (anti-enumeration). The password is never echoed, logged, or stored.</p>
 */
@RestController
@RequestMapping("/api/sessions")
public class SignInController {

    private final SignInService signInService;

    public SignInController(SignInService signInService) {
        this.signInService = signInService;
    }

    /** Authenticates and establishes a session. */
    @PostMapping
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) {
        SignInResult result = signInService.signIn(
            new SignInCommand(request.email(), request.password()));
        SignInResponse response = new SignInResponse(
            result.sessionId(), result.userId(), result.email(), result.role().label(),
            result.token().rawToken(), result.expiresAtEpochSecond());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> onInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.simple("UNAUTHORIZED", "INVALID_CREDENTIALS", ex.getMessage()));
    }
}