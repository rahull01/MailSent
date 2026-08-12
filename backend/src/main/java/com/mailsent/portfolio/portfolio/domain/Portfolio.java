package com.mailsent.portfolio.portfolio.domain;

import java.util.List;

import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * Portfolio — the container of an Agency Organization's Client Relationships (PORT-001).
 *
 * <p>Authoritative definition (DOMAIN_MODEL / GLOSSARY): Portfolio is <em>"the set of client
 * organizations and their domains an agency is accountable for"</em>, and <em>"one portfolio
 * contains many clients; a client belongs to exactly one portfolio (its agency's)"</em>
 * (DOMAIN_MODEL cardinality). A Portfolio is therefore a <strong>projection/container</strong>
 * over the Agency Organization's {@link ClientRelationship}s — it has no independent identity,
 * table, or lifecycle; it is derived from {@code ClientRelationship.agencyOrganizationId}
 * (the existing client_relationships persistence).</p>
 *
 * <p><strong>Invariant (INV-09):</strong> a Portfolio may contain only relationships whose
 * agency is the portfolio's owning organization — it never holds another agency's client.
 * Each client Organization therefore appears in exactly one portfolio (its agency's).</p>
 */
public final class Portfolio {

    private final String agencyOrganizationId;
    private final List<ClientRelationship> relationships;

    private Portfolio(String agencyOrganizationId, List<ClientRelationship> relationships) {
        this.agencyOrganizationId = agencyOrganizationId;
        this.relationships = List.copyOf(relationships);
    }

    /**
     * Builds the Portfolio container for an Agency Organization from its relationships.
     *
     * @param agencyOrganizationId the owning Agency Organization's stable identity, required
     * @param relationships        the agency's Client Relationships, required (may be empty)
     * @throws IllegalArgumentException if the agency id is blank, the list is null, or any
     *                                  relationship belongs to a different agency (INV-09)
     */
    public static Portfolio of(String agencyOrganizationId, List<ClientRelationship> relationships) {
        if (agencyOrganizationId == null || agencyOrganizationId.isBlank()) {
            throw new IllegalArgumentException("An agency organization id is required for a portfolio.");
        }
        if (relationships == null) {
            throw new IllegalArgumentException("A portfolio must be built from its relationships.");
        }
        for (ClientRelationship relationship : relationships) {
            if (!agencyOrganizationId.equals(relationship.agencyOrganizationId())) {
                throw new IllegalArgumentException(
                    "A portfolio may only contain relationships of its own agency (INV-09).");
            }
        }
        return new Portfolio(agencyOrganizationId, relationships);
    }

    /** The owning Agency Organization's stable identity. */
    public String agencyOrganizationId() {
        return agencyOrganizationId;
    }

    /** The contained Client Relationships (the agency's clients). */
    public List<ClientRelationship> relationships() {
        return relationships;
    }

    /** The distinct Client Organization identities in this portfolio. */
    public List<String> clientOrganizationIds() {
        return relationships.stream().map(ClientRelationship::clientOrganizationId).distinct().toList();
    }
}