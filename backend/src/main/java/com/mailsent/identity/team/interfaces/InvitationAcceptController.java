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
import com.mailsent.identity.team.domain.InvitationAlreadyAcceptedException;
import com.mailsent.identity.team.domain.InvalidInvitationTokenException;
import com.mailsent.identity.team.domain.InviteeNotFoundException;
import com.mailsent.identity.team.domain.Membership;
import com.mailsent.identity.team.domain.MembershipAlreadyExistsException;

/**
 * InvitationAcceptController — the single HTTP accept surface for TEAM-001.
 *
 * <p>Posession of a valid, unused acceptance token authorizes acceptance; no authenticated
 * principal is required here (the current-user contract is the deferred AUTH-006). Errors
 * map to business-first {@link ApiError}s with no implementation leakage, following the API
 * error philosophy.</p>
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationAcceptController {

    private final InvitationService invitationService;

    public InvitationAcceptController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /** Accepts an invitation and returns the membership established. */
    @PostMapping("/accept")
    public ResponseEntity<AcceptInvitationResponse> accept(@Valid @RequestBody AcceptInvitationRequest request) {
        Membership membership = invitationService.accept(request.token());
        AcceptInvitationResponse response = new AcceptInvitationResponse(membership.id(), membership.userId(),
            membership.organizationId(), membership.role().label());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(InvalidInvitationTokenException.class)
    public ResponseEntity<ApiError> onInvalidToken(InvalidInvitationTokenException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.simple("NOT_FOUND", "INVALID_INVITATION_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(InvitationAlreadyAcceptedException.class)
    public ResponseEntity<ApiError> onAlreadyAccepted(InvitationAlreadyAcceptedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "INVITATION_ALREADY_ACCEPTED", ex.getMessage()));
    }

    @ExceptionHandler(MembershipAlreadyExistsException.class)
    public ResponseEntity<ApiError> onMembershipExists(MembershipAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "MEMBERSHIP_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(InviteeNotFoundException.class)
    public ResponseEntity<ApiError> onInviteeMissing(InviteeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.simple("CONFLICT", "ACCOUNT_REQUIRED", ex.getMessage()));
    }
}