package com.mailsent.identity.consent.interfaces;

import java.time.Instant;

import com.mailsent.identity.consent.domain.Consent;

/**
 * ConsentResponse — the HTTP response DTO representing a recorded consent.
 *
 * <p>Decouples the external REST contract from internal domain entity details.</p>
 */
public record ConsentResponse(
    String id,
    String organizationId,
    String domainId,
    String grantedByUserId,
    String actionType,
    String status,
    Instant grantedAt,
    Instant revokedAt
) {

    public static ConsentResponse fromDomain(Consent consent) {
        return new ConsentResponse(
            consent.getId(),
            consent.getOrganizationId(),
            consent.getDomainId(),
            consent.getGrantedByUserId(),
            consent.getActionType().name(),
            consent.getStatus().name(),
            consent.getGrantedAt(),
            consent.getRevokedAt()
        );
    }
}
