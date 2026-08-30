package com.mailsent.identity.consent.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.mailsent.common.error.ApiError;
import com.mailsent.common.security.AuthenticatedIdentity;
import com.mailsent.common.security.TenantContext;
import com.mailsent.identity.consent.application.ConsentAlreadyExistsException;
import com.mailsent.identity.consent.application.ConsentService;
import com.mailsent.identity.consent.domain.Consent;

/**
 * ConsentController — the hardened action consent boundary (REM-SEC-001).
 *
 * <p>Enforces multi-tenant isolation (INV-09) and least-authority role access.
 * Caller organization identity and user identity are strictly extracted from
 * {@link TenantContext}, eliminating all BOLA/IDOR vulnerabilities.</p>
 */
@RestController
@RequestMapping("/api/v1/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping
    public ResponseEntity<ConsentResponse> grantConsent(@Valid @RequestBody GrantConsentRequest request) {
        AuthenticatedIdentity auth = TenantContext.getRequired();
        Consent consent = consentService.grantConsent(
            auth.organizationId(),
            request.domainId(),
            auth.userId(),
            request.actionType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsentResponse.fromDomain(consent));
    }

    @DeleteMapping("/{consentId}")
    public ResponseEntity<ConsentResponse> revokeConsent(@PathVariable String consentId) {
        AuthenticatedIdentity auth = TenantContext.getRequired();
        Consent consent = consentService.revokeConsent(
            consentId,
            auth.organizationId(),
            auth.userId()
        );
        return ResponseEntity.ok(ConsentResponse.fromDomain(consent));
    }

    @ExceptionHandler(ConsentAlreadyExistsException.class)
    public ResponseEntity<ApiError> onConsentAlreadyExists(ConsentAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "CONSENT_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> onSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError.simple("FORBIDDEN", "INSUFFICIENT_PERMISSIONS", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> onIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(ApiError.simple("BAD_REQUEST", "INVALID_REQUEST", ex.getMessage()));
    }
}
