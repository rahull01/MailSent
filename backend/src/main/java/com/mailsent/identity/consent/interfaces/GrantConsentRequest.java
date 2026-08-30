package com.mailsent.identity.consent.interfaces;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.mailsent.identity.consent.domain.ActionType;

/**
 * GrantConsentRequest — the hardened HTTP request DTO for granting action consent.
 *
 * <p>Contains only the target domain and action type. Caller's organization identity
 * and user identity are strictly extracted from {@code TenantContext} to prevent
 * BOLA/IDOR vulnerabilities (INV-09).</p>
 */
public record GrantConsentRequest(
    @NotBlank(message = "domainId is required")
    String domainId,

    @NotNull(message = "actionType is required")
    ActionType actionType
) {}
