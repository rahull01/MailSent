package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * HealthSignal — an immutable unified delivery-health snapshot (HEALTH-001).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not Trust Score,
 * not Health Trend, not an Issue, not Alignment, and not DIAG-001 memory.
 * Tenant binding is derived from the registered {@link Domain}. Components are
 * cited source snapshot ids (Observation Evidence, DMARC Aggregate Evidence
 * actually used, Reputation Observation actually used), not separately
 * persisted health scores.</p>
 *
 * <p>Factory-based creation only ({@link #derive}, {@link #restore}). No
 * setters, no update, no delete. A later derivation of the same domain is a
 * new snapshot. Current signal is the latest by {@code observedAt}.</p>
 */
public final class HealthSignal {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final HealthIndication indication;
    private final String observationEvidenceId;
    private final List<String> dmarcAggregateEvidenceIds;
    private final List<String> reputationObservationIds;
    private final Instant observedAt;

    private HealthSignal(String id, String organizationId, String domainId, String domainName,
                         HealthIndication indication, String observationEvidenceId,
                         List<String> dmarcAggregateEvidenceIds, List<String> reputationObservationIds,
                         Instant observedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.indication = indication;
        this.observationEvidenceId = observationEvidenceId;
        this.dmarcAggregateEvidenceIds = List.copyOf(dmarcAggregateEvidenceIds);
        this.reputationObservationIds = List.copyOf(reputationObservationIds);
        this.observedAt = observedAt == null ? null : observedAt.truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * Derives one Health Signal envelope against the registered domain.
     * Identity and tenant fields are taken from {@code domain}; cited ids are
     * the source snapshots actually used.
     */
    public static HealthSignal derive(Domain domain, HealthIndication indication, String observationEvidenceId,
                                      List<String> dmarcAggregateEvidenceIds,
                                      List<String> reputationObservationIds, Instant observedAt) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (indication == null) {
            throw new IllegalArgumentException("A health indication is required.");
        }
        if (observationEvidenceId == null || observationEvidenceId.isBlank()) {
            throw new IllegalArgumentException("An Observation Evidence id is required.");
        }
        if (dmarcAggregateEvidenceIds == null || dmarcAggregateEvidenceIds.isEmpty()) {
            throw new IllegalArgumentException("DMARC Aggregate Evidence ids actually used are required.");
        }
        if (reputationObservationIds == null || reputationObservationIds.isEmpty()) {
            throw new IllegalArgumentException("Reputation Observation ids actually used are required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        return new HealthSignal(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            indication,
            observationEvidenceId,
            dmarcAggregateEvidenceIds,
            reputationObservationIds,
            observedAt);
    }

    /**
     * Reconstructs a Health Signal from durable storage, adopting stored
     * identity, tenant binding, indication, lineage, and observation instant
     * verbatim.
     */
    public static HealthSignal restore(String id, String organizationId, String domainId, String domainName,
                                       HealthIndication indication, String observationEvidenceId,
                                       List<String> dmarcAggregateEvidenceIds,
                                       List<String> reputationObservationIds, Instant observedAt) {
        return new HealthSignal(
            id, organizationId, domainId, domainName, indication, observationEvidenceId,
            dmarcAggregateEvidenceIds, reputationObservationIds, observedAt);
    }

    public String id() {
        return id;
    }

    public String organizationId() {
        return organizationId;
    }

    public String domainId() {
        return domainId;
    }

    public String domainName() {
        return domainName;
    }

    public HealthIndication indication() {
        return indication;
    }

    public String observationEvidenceId() {
        return observationEvidenceId;
    }

    public List<String> dmarcAggregateEvidenceIds() {
        return dmarcAggregateEvidenceIds;
    }

    public List<String> reputationObservationIds() {
        return reputationObservationIds;
    }

    public Instant observedAt() {
        return observedAt;
    }
}
