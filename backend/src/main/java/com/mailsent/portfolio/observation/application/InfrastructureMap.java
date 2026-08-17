package com.mailsent.portfolio.observation.application;

import java.util.List;

/**
 * Infrastructure Map — MAP-001 topology read-model for one registered Domain.
 *
 * <p>Not an aggregate, not a persisted table, not Health Signal, and not
 * MAP-002–004 (color, detail panel, export). Tenant fields come from the
 * registered Domain.</p>
 */
public record InfrastructureMap(
    String organizationId,
    String domainId,
    String domainName,
    List<InfrastructureMapNode> nodes,
    List<InfrastructureMapRelationship> relationships,
    List<InfrastructureEvidenceRef> dnsEvidence
) {
    public InfrastructureMap {
        nodes = List.copyOf(nodes);
        relationships = List.copyOf(relationships);
        dnsEvidence = List.copyOf(dnsEvidence);
    }
}
