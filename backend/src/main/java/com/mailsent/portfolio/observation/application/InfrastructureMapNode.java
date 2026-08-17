package com.mailsent.portfolio.observation.application;

/**
 * One node of the Infrastructure Map topology read-model (MAP-001). Identity,
 * type, and availability only — no score, color, health, or severity.
 */
public record InfrastructureMapNode(
    String identity,
    InfrastructureNodeType type,
    NodeAvailability availability
) {
}
