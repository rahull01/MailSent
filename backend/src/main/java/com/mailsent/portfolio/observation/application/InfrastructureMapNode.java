package com.mailsent.portfolio.observation.application;

import java.util.Objects;
import com.mailsent.portfolio.observation.domain.HealthIndication;

/**
 * One node of the Infrastructure Map topology read-model (MAP-001 / MAP-002).
 * Exposes identity, type, observation availability, and verified health indication.
 */
public record InfrastructureMapNode(
    String identity,
    InfrastructureNodeType type,
    NodeAvailability availability,
    HealthIndication health
) {
    public InfrastructureMapNode {
        Objects.requireNonNull(identity, "Node identity is required");
        Objects.requireNonNull(type, "Node type is required");
        Objects.requireNonNull(availability, "Node availability is required");
        if (health == null) {
            health = HealthIndication.UNKNOWN;
        }
    }

    public InfrastructureMapNode(String identity, InfrastructureNodeType type, NodeAvailability availability) {
        this(identity, type, availability, HealthIndication.UNKNOWN);
    }
}
