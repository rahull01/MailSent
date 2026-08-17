package com.mailsent.portfolio.observation.application;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;

/**
 * HealthSignalService — the HEALTH-001 application boundary.
 *
 * <p>Derives one unified Health Signal envelope for a registered Domain from
 * the latest persisted Observation Evidence, DMARC Aggregate Evidence (latest
 * report), and Reputation Observation snapshots. The write use case accepts
 * only a domain name; organization and domain identity are derived from the
 * registered {@link Domain}. Missing any V1 required source persists nothing
 * (INV-01). When required snapshots exist, indication is assigned from cited
 * V15 tokens only ({@code disposition}, {@code spf_result}, {@code dkim_result},
 * {@code spf_aligned}, {@code dkim_aligned}). Observation Evidence and
 * Reputation Observation are lineage presence, not voters. DIAG-001 Alignment
 * is not an input.</p>
 */
public class HealthSignalService {

    private final HealthSignalRepository signals;
    private final DomainRepository domains;
    private final ObservationEvidenceRepository observationEvidence;
    private final DmarcAggregateEvidenceRepository aggregateEvidence;
    private final ReputationObservationRepository reputationObservations;
    private final Clock clock;

    public HealthSignalService(HealthSignalRepository signals, DomainRepository domains,
                               ObservationEvidenceRepository observationEvidence,
                               DmarcAggregateEvidenceRepository aggregateEvidence,
                               ReputationObservationRepository reputationObservations, Clock clock) {
        this.signals = signals;
        this.domains = domains;
        this.observationEvidence = observationEvidence;
        this.aggregateEvidence = aggregateEvidence;
        this.reputationObservations = reputationObservations;
        this.clock = clock;
    }

    /**
     * Derives and, when the cited source set differs from the latest snapshot,
     * persists a Health Signal for a registered domain.
     *
     * @param domainName the domain to derive (canonicalized)
     * @return the newly persisted snapshot, or empty when a required source is
     *         missing or the cited ids match the latest snapshot
     * @throws DomainNotRegisteredException if the domain is not registered
     */
    public Optional<HealthSignal> record(String domainName) {
        return derive(domainName);
    }

    /**
     * Explicit in-process derivation alias of {@link #record(String)}.
     */
    public Optional<HealthSignal> derive(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));

        Optional<ObservationEvidence> latestEvidence = latestObservationEvidence(domain.id());
        List<DmarcAggregateEvidence> latestReport = latestAggregateReport(domain.id());
        List<ReputationObservation> latestReputation = latestReputationBySource(domain.id());
        if (latestEvidence.isEmpty() || latestReport.isEmpty() || latestReputation.isEmpty()) {
            return Optional.empty();
        }

        String observationEvidenceId = latestEvidence.get().id();
        List<String> aggregateIds = sortedIds(latestReport.stream().map(DmarcAggregateEvidence::id).toList());
        List<String> reputationIds = sortedIds(latestReputation.stream().map(ReputationObservation::id).toList());

        Optional<HealthSignal> current = latestSignal(domain.id());
        if (current.isPresent() && sameCitedSources(current.get(), observationEvidenceId, aggregateIds, reputationIds)) {
            return Optional.empty();
        }

        Instant observedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);
        return Optional.of(signals.save(HealthSignal.derive(
            domain,
            indicationFrom(latestReport),
            observationEvidenceId,
            aggregateIds,
            reputationIds,
            observedAt)));
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<HealthSignal> findById(String id) {
        return signals.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<HealthSignal> findByDomainId(String domainId) {
        return signals.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<HealthSignal> findByOrganizationId(String organizationId) {
        return signals.findByOrganizationId(organizationId);
    }

    private Optional<ObservationEvidence> latestObservationEvidence(String domainId) {
        return observationEvidence.findByDomainId(domainId).stream()
            .max(Comparator.comparing(ObservationEvidence::observedAt)
                .thenComparing(ObservationEvidence::id));
    }

    /**
     * All rows of the latest DMARC report for the domain: the report whose
     * {@code utc_day} is greatest (null days only when no dated row exists),
     * then a stable {@code report_id} among that day.
     */
    private List<DmarcAggregateEvidence> latestAggregateReport(String domainId) {
        List<DmarcAggregateEvidence> all = aggregateEvidence.findByDomainId(domainId);
        if (all.isEmpty()) {
            return List.of();
        }
        LocalDate latestDay = all.stream()
            .map(DmarcAggregateEvidence::utcDay)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(null);
        List<DmarcAggregateEvidence> onLatestDay = latestDay == null
            ? all
            : all.stream().filter(snapshot -> latestDay.equals(snapshot.utcDay())).toList();
        String reportId = onLatestDay.stream()
            .map(DmarcAggregateEvidence::reportId)
            .max(String::compareTo)
            .orElse(null);
        if (reportId == null) {
            return List.of();
        }
        return all.stream().filter(snapshot -> reportId.equals(snapshot.reportId())).toList();
    }

    private List<ReputationObservation> latestReputationBySource(String domainId) {
        List<ReputationObservation> all = reputationObservations.findByDomainId(domainId);
        List<ReputationObservation> latest = new ArrayList<>();
        for (ReputationSource source : ReputationSource.values()) {
            all.stream()
                .filter(observation -> observation.source() == source)
                .max(Comparator.comparing(ReputationObservation::observedAt)
                    .thenComparing(ReputationObservation::id))
                .ifPresent(latest::add);
        }
        return latest;
    }

    private Optional<HealthSignal> latestSignal(String domainId) {
        return signals.findByDomainId(domainId).stream()
            .max(Comparator.comparing(HealthSignal::observedAt).thenComparing(HealthSignal::id));
    }

    /**
     * At-risk if any cited row has disposition quarantine/reject or any of the
     * four auth/alignment fields fail. Healthy only if every cited row has
     * disposition none and all four auth/alignment fields pass. Otherwise
     * unknown. Tokens are compared trimmed and case-insensitive.
     */
    private static HealthIndication indicationFrom(List<DmarcAggregateEvidence> rows) {
        boolean allHealthy = true;
        for (DmarcAggregateEvidence row : rows) {
            if (isAtRiskRow(row)) {
                return HealthIndication.AT_RISK;
            }
            if (!isHealthyRow(row)) {
                allHealthy = false;
            }
        }
        return allHealthy ? HealthIndication.HEALTHY : HealthIndication.UNKNOWN;
    }

    private static boolean isAtRiskRow(DmarcAggregateEvidence row) {
        String disposition = token(row.disposition());
        return "quarantine".equals(disposition)
            || "reject".equals(disposition)
            || "fail".equals(token(row.spfResult()))
            || "fail".equals(token(row.dkimResult()))
            || "fail".equals(token(row.spfAligned()))
            || "fail".equals(token(row.dkimAligned()));
    }

    private static boolean isHealthyRow(DmarcAggregateEvidence row) {
        return "none".equals(token(row.disposition()))
            && "pass".equals(token(row.spfResult()))
            && "pass".equals(token(row.dkimResult()))
            && "pass".equals(token(row.spfAligned()))
            && "pass".equals(token(row.dkimAligned()));
    }

    private static String token(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean sameCitedSources(HealthSignal current, String observationEvidenceId,
                                            List<String> aggregateIds, List<String> reputationIds) {
        return current.observationEvidenceId().equals(observationEvidenceId)
            && setOf(current.dmarcAggregateEvidenceIds()).equals(setOf(aggregateIds))
            && setOf(current.reputationObservationIds()).equals(setOf(reputationIds));
    }

    private static List<String> sortedIds(List<String> ids) {
        return ids.stream().sorted().toList();
    }

    private static Set<String> setOf(List<String> ids) {
        return new HashSet<>(ids);
    }
}
