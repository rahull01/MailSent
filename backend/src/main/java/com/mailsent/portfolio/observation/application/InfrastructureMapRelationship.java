package com.mailsent.portfolio.observation.application;

/**
 * A typed from–to edge of the Infrastructure Map topology (MAP-001).
 */
public record InfrastructureMapRelationship(
    String fromIdentity,
    String toIdentity,
    InfrastructureRelationshipType type
) {
}
