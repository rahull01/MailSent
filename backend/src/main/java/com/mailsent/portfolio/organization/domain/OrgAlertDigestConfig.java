package com.mailsent.portfolio.organization.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * OrgAlertDigestConfig — Organization-level alert and digest frequency settings (SET-001).
 */
public class OrgAlertDigestConfig {

    public enum DigestCadence {
        DAILY,
        WEEKLY,
        OFF
    }

    private final String id;
    private final String organizationId;
    private DigestCadence cadence;
    private String digestRecipientEmail;
    private boolean notifyOnCriticalOnly;

    public OrgAlertDigestConfig(String id, String organizationId, DigestCadence cadence,
                                String digestRecipientEmail, boolean notifyOnCriticalOnly) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.cadence = Objects.requireNonNull(cadence);
        this.digestRecipientEmail = Objects.requireNonNull(digestRecipientEmail);
        this.notifyOnCriticalOnly = notifyOnCriticalOnly;
    }

    public static OrgAlertDigestConfig defaultConfig(String organizationId, String defaultEmail) {
        return new OrgAlertDigestConfig(
                UUID.randomUUID().toString(),
                organizationId,
                DigestCadence.DAILY,
                defaultEmail,
                false
        );
    }

    public void update(DigestCadence cadence, String email, boolean notifyOnCriticalOnly) {
        this.cadence = Objects.requireNonNull(cadence);
        this.digestRecipientEmail = Objects.requireNonNull(email);
        this.notifyOnCriticalOnly = notifyOnCriticalOnly;
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public DigestCadence cadence() { return cadence; }
    public String digestRecipientEmail() { return digestRecipientEmail; }
    public boolean notifyOnCriticalOnly() { return notifyOnCriticalOnly; }
}
