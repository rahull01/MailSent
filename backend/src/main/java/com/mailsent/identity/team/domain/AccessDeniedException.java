package com.mailsent.identity.team.domain;

import java.util.Objects;

/**
 * AccessDeniedException — raised when a user attempts an unauthorized capability or crosses a tenant boundary.
 */
public class AccessDeniedException extends RuntimeException {

    private final String userId;
    private final String organizationId;
    private final Capability attemptedCapability;

    public AccessDeniedException(String message) {
        super(message);
        this.userId = null;
        this.organizationId = null;
        this.attemptedCapability = null;
    }

    public AccessDeniedException(String userId, String organizationId, Capability attemptedCapability, String message) {
        super(message);
        this.userId = userId;
        this.organizationId = organizationId;
        this.attemptedCapability = attemptedCapability;
    }

    public String userId() {
        return userId;
    }

    public String organizationId() {
        return organizationId;
    }

    public Capability attemptedCapability() {
        return attemptedCapability;
    }
}
