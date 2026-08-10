package com.mailsent.identity.team.interfaces;

import jakarta.validation.constraints.NotBlank;

/**
 * AcceptInvitationRequest — the inbound acceptance contract for TEAM-001. Carries the raw
 * acceptance token, which is validated (not blank) here and resolved by the domain on
 * acceptance. The token is a bearer credential and never appears in a persisted form.
 *
 * @param token the raw acceptance token delivered to the invitee
 */
public record AcceptInvitationRequest(
    @NotBlank(message = "An invitation token is required.") String token
) {
}