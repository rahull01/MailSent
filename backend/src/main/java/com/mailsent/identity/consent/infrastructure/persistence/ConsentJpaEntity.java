package com.mailsent.identity.consent.infrastructure.persistence;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "consents")
public class ConsentJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 200, nullable = false)
    private String organizationId;

    @Column(name = "domain_id", length = 200, nullable = false)
    private String domainId;

    @Column(name = "granted_by_user_id", length = 200, nullable = false)
    private String grantedByUserId;

    @Column(name = "action_type", length = 50, nullable = false)
    private String actionType;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected ConsentJpaEntity() {}

    public ConsentJpaEntity(String id, String organizationId, String domainId, String grantedByUserId, String actionType, String status, Instant grantedAt, Instant revokedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.grantedByUserId = grantedByUserId;
        this.actionType = actionType;
        this.status = status;
        this.grantedAt = grantedAt;
        this.revokedAt = revokedAt;
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public String grantedByUserId() { return grantedByUserId; }
    public String actionType() { return actionType; }
    public String status() { return status; }
    public Instant grantedAt() { return grantedAt; }
    public Instant revokedAt() { return revokedAt; }
}
