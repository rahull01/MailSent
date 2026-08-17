package com.mailsent.identity.consent.domain;

import java.time.Instant;
import java.util.UUID;

public class Consent {
    private String id;
    private String organizationId;
    private String domainId;
    private String grantedByUserId;
    private ActionType actionType;
    private Status status;
    private Instant grantedAt;
    private Instant revokedAt;

    private Consent() {
    }

    public static Consent grant(String organizationId, String domainId, String grantedByUserId, ActionType actionType) {
        Consent consent = new Consent();
        consent.id = UUID.randomUUID().toString();
        consent.organizationId = organizationId;
        consent.domainId = domainId;
        consent.grantedByUserId = grantedByUserId;
        consent.actionType = actionType;
        consent.status = Status.GRANTED;
        consent.grantedAt = Instant.now();
        return consent;
    }

    public static Consent restore(String id, String organizationId, String domainId, String grantedByUserId, ActionType actionType, Status status, Instant grantedAt, Instant revokedAt) {
        Consent consent = new Consent();
        consent.id = id;
        consent.organizationId = organizationId;
        consent.domainId = domainId;
        consent.grantedByUserId = grantedByUserId;
        consent.actionType = actionType;
        consent.status = status;
        consent.grantedAt = grantedAt;
        consent.revokedAt = revokedAt;
        return consent;
    }

    public void revoke(String revokedByUserId, Instant revokedAt) {
        this.status = Status.REVOKED;
        this.revokedAt = revokedAt;
    }

    public String getId() { return id; }
    public String getOrganizationId() { return organizationId; }
    public String getDomainId() { return domainId; }
    public String getGrantedByUserId() { return grantedByUserId; }
    public ActionType getActionType() { return actionType; }
    public Status getStatus() { return status; }
    public Instant getGrantedAt() { return grantedAt; }
    public Instant getRevokedAt() { return revokedAt; }
}
