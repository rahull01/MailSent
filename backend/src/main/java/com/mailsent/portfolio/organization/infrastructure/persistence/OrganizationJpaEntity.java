package com.mailsent.portfolio.organization.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.organization.domain.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * OrganizationJpaEntity — durable persistence mapping for the {@link Organization}
 * aggregate. A persistence concern, not a duplicate domain model. Stores the stable
 * identity, name, formation time, and lifecycle state (ACTIVE/CLOSED + closure time).</p>
 */
@Entity
@Table(name = "organizations")
public class OrganizationJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private Organization.Status status;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected OrganizationJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public OrganizationJpaEntity(String id, String name, Instant createdAt,
                                 Organization.Status status, Instant closedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.status = status;
        this.closedAt = closedAt;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Organization.Status status() {
        return status;
    }

    public Instant closedAt() {
        return closedAt;
    }
}