package com.mailsent.portfolio.organization.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Subscription — Tracks an organization's subscription state and billing lifecycle (BILL-002).
 */
public class Subscription {

    public enum Status {
        TRIAL,
        ACTIVE,
        PAST_DUE,
        CANCELED
    }

    private final String id;
    private final String organizationId;
    private PlanTier tier;
    private Status status;
    private boolean annual;
    private Instant currentPeriodEnd;

    private Subscription(String id, String organizationId, PlanTier tier,
                         Status status, boolean annual, Instant currentPeriodEnd) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.tier = Objects.requireNonNull(tier);
        this.status = Objects.requireNonNull(status);
        this.annual = annual;
        this.currentPeriodEnd = Objects.requireNonNull(currentPeriodEnd);
    }

    public static Subscription startTrial(String organizationId, Instant trialEnd) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        return new Subscription(
                UUID.randomUUID().toString(),
                organizationId,
                PlanTier.AGENCY_PRO,
                Status.TRIAL,
                false,
                trialEnd != null ? trialEnd : Instant.now().plusSeconds(14 * 86400)
        );
    }

    public static Subscription create(String organizationId, PlanTier tier, boolean annual, Instant periodEnd) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        return new Subscription(
                UUID.randomUUID().toString(),
                organizationId,
                tier,
                Status.ACTIVE,
                annual,
                periodEnd
        );
    }

    public void upgrade(PlanTier newTier, boolean annual, Instant newPeriodEnd) {
        this.tier = Objects.requireNonNull(newTier);
        this.annual = annual;
        this.status = Status.ACTIVE;
        this.currentPeriodEnd = Objects.requireNonNull(newPeriodEnd);
    }

    public void cancel() {
        this.status = Status.CANCELED;
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public PlanTier tier() { return tier; }
    public Status status() { return status; }
    public boolean isAnnual() { return annual; }
    public Instant currentPeriodEnd() { return currentPeriodEnd; }

    public boolean isActiveOrTrial() {
        return status == Status.ACTIVE || status == Status.TRIAL;
    }
}
