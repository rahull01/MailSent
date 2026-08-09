package com.mailsent.portfolio.domain.infrastructure.persistence;

import java.time.Instant;

import com.mailsent.portfolio.domain.domain.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * DomainJpaEntity — durable persistence mapping for the {@link Domain} aggregate. A
 * persistence concern, not a duplicate domain model. Stores the stable identity, the
 * single owning organization, the canonical name, and the registration instant. The
 * {@code NOT NULL} owning-organization and global name uniqueness are enforced at the
 * schema level ({@code V5__create_domains.sql}).
 */
@Entity
@Table(name = "domains")
public class DomainJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "name", length = 253, nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DomainJpaEntity() {
        // JPA requires a no-arg constructor.
    }

    public DomainJpaEntity(String id, String organizationId, String name, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String id() {
        return id;
    }

    public String organizationId() {
        return organizationId;
    }

    public String name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }
}