package com.mailsent.portfolio.client.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.client.domain.ClientRelationship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ClientRelationshipJpaEntity — durable persistence mapping for the {@link ClientRelationship}
 * aggregate. A persistence concern, not a duplicate domain model. Stores the Agency and the
 * Client organization references (existing organizations), the formation time, and the lifecycle
 * state (ACTIVE → ENDED, {@code ended_at} NULL while ACTIVE). The owning-organization FKs are
 * enforced at the schema level (V7, INV-14); the relationship references organizations only.
 */
@Entity
@Table(name = "client_relationships")
public class ClientRelationshipJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "agency_organization_id", length = 36, nullable = false)
    private String agencyOrganizationId;

    @Column(name = "client_organization_id", length = 36, nullable = false)
    private String clientOrganizationId;

    @Column(name = "formed_at", nullable = false)
    private Instant formedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private ClientRelationship.Status status;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected ClientRelationshipJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public ClientRelationshipJpaEntity(String id, String agencyOrganizationId, String clientOrganizationId,
                                       Instant formedAt, ClientRelationship.Status status, Instant endedAt) {
        this.id = id;
        this.agencyOrganizationId = agencyOrganizationId;
        this.clientOrganizationId = clientOrganizationId;
        this.formedAt = formedAt;
        this.status = status;
        this.endedAt = endedAt;
    }

    public String id() {
        return id;
    }

    public String agencyOrganizationId() {
        return agencyOrganizationId;
    }

    public String clientOrganizationId() {
        return clientOrganizationId;
    }

    public Instant formedAt() {
        return formedAt;
    }

    public ClientRelationship.Status status() {
        return status;
    }

    public Instant endedAt() {
        return endedAt;
    }
}