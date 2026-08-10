package com.mailsent.identity.team.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.mailsent.common.error.ApiError;
import com.mailsent.identity.team.application.InvitationService;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.domain.InvitationAlreadyAcceptedException;
import com.mailsent.identity.team.domain.InvalidInvitationTokenException;
import com.mailsent.identity.team.domain.InviteeNotFoundException;
import com.mailsent.identity.team.domain.MalformedInvitationTokenException;
import com.mailsent.identity.team.domain.OrganizationMismatchException;

/**
 * InvitationAcceptController — the single HTTP accept surface for TEAM-001 (frozen v8 §11).
 *
 * <p>Possession of a valid acceptance token is the acceptance authority; no authenticated
 * principal is required and AUTH-006 is not a dependency. Errors map to business-first
 * {@link ApiError}s that never reveal invitation metadata (no invitee email, organization
 * id/name, role, creation/accepted time, or token hash). Invalid request bodies are handled
 * by the existing validation mapping ({@code 400 VALIDATION_FAILED}).</p>
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationAcceptController {

    private final InvitationService invitationService;

    public InvitationAcceptController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /** Accepts an invitation and returns the exact success body. */
    @PostMapping("/accept")
    public ResponseEntity<AcceptInvitationResponse> accept(@Valid @RequestBody AcceptInvitationRequest request) {
        Invitation invitation = invitationService.accept(request.token());
        return ResponseEntity.ok(new AcceptInvitationResponse(true, invitation.id()));
    }

    @ExceptionHandler(MalformedInvitationTokenException.class)
    public ResponseEntity<ApiError> onMalformedToken(MalformedInvitationTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError.simple("BAD_REQUEST", "INVALID_INVITATION_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(InvalidInvitationTokenException.class)
    public ResponseEntity<ApiError> onUnknownToken(InvalidInvitationTokenException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.simple("NOT_FOUND", "INVALID_INVITATION_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(InvitationAlreadyAcceptedException.class)
    public ResponseEntity<ApiError> onAlreadyAccepted(InvitationAlreadyAcceptedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "INVITATION_ALREADY_ACCEPTED", ex.getMessage()));
    }

    @ExceptionHandler(InviteeNotFoundException.class)
    public ResponseEntity<ApiError> onInviteeMissing(InviteeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "ACCOUNT_REQUIRED", ex.getMessage()));
    }

    @ExceptionHandler(OrganizationMismatchException.class)
    public ResponseEntity<ApiError> onOrgMismatch(OrganizationMismatchException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "ORG_MISMATCH", ex.getMessage()));
    }
}