package com.mailsent.portfolio.observation.application;

/**
 * Kind of optional snapshot referenced behind the DNS node (MAP-001). These are
 * evidence references, not extra topology node types.
 */
public enum InfrastructureEvidenceKind {
    OBSERVATION_EVIDENCE,
    REPUTATION_OBSERVATION,
    DNS_HEALTH_OBSERVATION,
    BLACKLIST_OBSERVATION
}
