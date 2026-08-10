package com.mailsent.portfolio.client.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * ClientRelationship — an Agency ↔ Client relationship (CLIENT-001).
 *
 * <p>Mirrors the DATA_MODEL "Client Relationship" aggregate (root <em>Client</em>): it
 * "holds the relationship between an agency and a client." An Agency is an {@code Organization}
 * acting in an agency capacity; a Client is a distinct {@code Organization} "served by an
 * agency" (DOMAIN_MODEL). The relationship is organization-to-organization only — it never
 * references a User, so it can never become a user-membership mechanism (TEAM-001 frozen),
 * and it never alters either organization's identity or lifecycle.</p>
 *
 * <p><strong>Lifecycle</strong> is formed → sustained → ended (DATA_MODEL). {@code ACTIVE}
 * on formation ({@code formedAt} set, {@code endedAt} null); {@code end(endedAt)}
 * transitions {@code ACTIVE → ENDED} irreversibly. There is no physical deletion and no
 * generic update (CLIENT-001 contract).</p>
 */
public final class ClientRelationship {

    /** Lifecycle state: ACTIVE → ENDED only. */
    public enum Status {
        ACTIVE,
        ENDED
    }

    private final String id;
    private final String agencyOrganizationId;
    private final String clientOrganizationId;
    private final Instant formedAt;
    private Status status;
    private Instant endedAt;

    private ClientRelationship(String id, String agencyOrganizationId, String clientOrganizationId,
                               Instant formedAt, Status status, Instant endedAt) {
        this.id = id;
        this.agencyOrganizationId = agencyOrganizationId;
        this.clientOrganizationId = clientOrganizationId;
        this.formedAt = formedAt;
        this.status = status;
        this.endedAt = endedAt;
    }

    /**
     * Forms a new ACTIVE relationship between an existing Agency Organization and an existing
     * Client Organization.
     *
     * @param agencyOrganizationId the Agency Organization's stable identity, required
     * @param clientOrganizationId the Client Organization's stable identity, required
     * @param formedAt             the instant the relationship is formed, required
     * @throws IllegalArgumentException if either organization id is missing, {@code formedAt}
     *                                  is null, or the two organizations are the same
     */
    public static ClientRelationship form(String agencyOrganizationId, String clientOrganizationId,
                                          Instant formedAt) {
        if (agencyOrganizationId == null || agencyOrganizationId.isBlank()) {
            throw new IllegalArgumentException("An agency organization id is required.");
        }
        if (clientOrganizationId == null || clientOrganizationId.isBlank()) {
            throw new IllegalArgumentException("A client organization id is required.");
        }
        if (formedAt == null) {
            throw new IllegalArgumentException("A formation time is required.");
        }
        if (agencyOrganizationId.equals(clientOrganizationId)) {
            throw new IllegalArgumentException("An organization cannot be its own client.");
        }
        return new ClientRelationship(UUID.randomUUID().toString(), agencyOrganizationId,
            clientOrganizationId, formedAt, Status.ACTIVE, null);
    }

    /**
     * Reconstructs a relationship from durable storage, adopting its stored state verbatim.
     * Used only by the persistence adapter (never reassigns identity).
     */
    public static ClientRelationship restore(String id, String agencyOrganizationId, String clientOrganizationId,
                                             Instant formedAt, Status status, Instant endedAt) {
        return new ClientRelationship(id, agencyOrganizationId, clientOrganizationId, formedAt, status, endedAt);
    }

    /**
     * Ends the relationship: {@code ACTIVE → ENDED}, recording the end instant. Irreversible;
     * ending an already-ended relationship is a no-op (its {@code endedAt} is unchanged). There
     * is no physical deletion.
     *
     * @param endedAt the end instant, required
     */
    public void end(Instant endedAt) {
        if (endedAt == null) {
            throw new IllegalArgumentException("An end time is required.");
        }
        if (status == Status.ACTIVE) {
            this.status = Status.ENDED;
            this.endedAt = endedAt;
        }
    }

    /** The stable identity of the relationship (INV-10). */
    public String id() {
        return id;
    }

    /** The Agency Organization's stable identity. */
    public String agencyOrganizationId() {
        return agencyOrganizationId;
    }

    /** The Client Organization's stable identity. */
    public String clientOrganizationId() {
        return clientOrganizationId;
    }

    /** The instant the relationship was formed. */
    public Instant formedAt() {
        return formedAt;
    }

    /** The current lifecycle state (ACTIVE or ENDED). */
    public Status status() {
        return status;
    }

    /** The instant the relationship ended, or {@code null} while ACTIVE. */
    public Instant endedAt() {
        return endedAt;
    }
}