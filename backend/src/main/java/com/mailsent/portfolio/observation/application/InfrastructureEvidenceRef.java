package com.mailsent.portfolio.observation.application;

import java.time.Instant;

/**
 * Optional snapshot identity attached behind the DNS node (MAP-001). Not a
 * topology node and not a health conclusion.
 */
public record InfrastructureEvidenceRef(
    String snapshotId,
    InfrastructureEvidenceKind kind,
    Instant observedAt
) {
}
