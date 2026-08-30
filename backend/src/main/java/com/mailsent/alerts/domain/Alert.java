package com.mailsent.alerts.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Alert — ALERT-002 Business-language body (FUN-M4).
 *
 * <p>Represents a notification event intended for a user. FUN-M4 strictly dictates
 * that the alert must lead with business language rather than raw technical terms,
 * explicitly telling the user what happened, why it matters, and whether to act.
 * This aggregate root enforces those three semantic dimensions.</p>
 *
 * <p><strong>Tenant boundary (INV-09 / CTO decision D2).</strong> {@code organizationId}
 * is mandatory and is the authoritative tenant scope of the alert. {@code incidentId} is
 * nullable: per DOMAIN_MODEL ("an issue <em>or a health change</em> produces alerts"), an
 * alert may originate from a health change with no underlying incident, and no Incident
 * may be fabricated just to satisfy this field.</p>
 */
public class Alert {
    private final String id;
    private final String organizationId;
    private final String incidentId;
    private final AlertSeverity severity;
    private final String whatHappened;
    private final String whyItMatters;
    private final String actionRequired;
    private final Instant createdAt;

    private Alert(String id, String organizationId, String incidentId, AlertSeverity severity,
                  String whatHappened, String whyItMatters, String actionRequired,
                  Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = requireTenant(organizationId);
        this.incidentId = incidentId; // nullable: health-change-originated alerts have no incident
        this.severity = Objects.requireNonNull(severity);
        this.whatHappened = Objects.requireNonNull(whatHappened);
        this.whyItMatters = Objects.requireNonNull(whyItMatters);
        this.actionRequired = actionRequired; // Optional: null if no action is required
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * Generates a new Alert.
     *
     * @param organizationId the owning organization's stable identity (authoritative tenant scope), required
     * @param incidentId     the source incident, or null for health-change-originated alerts
     * @param severity       the urgency tier
     * @param whatHappened   business-language explanation of the event
     * @param whyItMatters   business-language explanation of the impact
     * @param actionRequired the required action (or null if purely informational)
     * @param createdAt      the deterministic timestamp of creation
     * @return a new Alert instance
     */
    public static Alert generate(String organizationId, String incidentId, AlertSeverity severity,
                                 String whatHappened, String whyItMatters,
                                 String actionRequired, Instant createdAt) {
        if (whatHappened == null || whatHappened.isBlank()) {
            throw new IllegalArgumentException("FUN-M4 violation: 'what happened' is strictly required.");
        }
        if (whyItMatters == null || whyItMatters.isBlank()) {
            throw new IllegalArgumentException("FUN-M4 violation: 'why it matters' is strictly required.");
        }
        return new Alert(UUID.randomUUID().toString(), organizationId, incidentId, severity,
                         whatHappened, whyItMatters, actionRequired, createdAt);
    }

    private static String requireTenant(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("An owning organization id is required for every alert (INV-09).");
        }
        return organizationId;
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String incidentId() { return incidentId; }
    public AlertSeverity severity() { return severity; }
    public String whatHappened() { return whatHappened; }
    public String whyItMatters() { return whyItMatters; }
    public String actionRequired() { return actionRequired; }
    public Instant createdAt() { return createdAt; }
}
