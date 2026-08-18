package com.mailsent.portfolio.domain.application;

import java.time.Instant;

/**
 * DomainSummary — Safe DTO projection of registered domain metadata (DOM-003).
 *
 * <p>Exposes only real, grounded, persisted domain attributes per FUN-D3.
 * Adheres strictly to N2 (Never Fabricate Truth) — no synthetic health or risk scores.</p>
 *
 * @param id             the stable domain identity (UUID)
 * @param organizationId the owning organization's stable identity
 * @param name           the canonical (lowercase) domain name
 * @param createdAt      the registration timestamp
 */
public record DomainSummary(
    String id,
    String organizationId,
    String name,
    Instant createdAt
) {}
