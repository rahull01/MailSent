package com.mailsent.portfolio.observation.application;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.ParsedDmarcRecord;
import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;

/**
 * DmarcAggregateEvidenceService — the INT-005 application boundary.
 *
 * <p>Aggregates a usable {@link ParsedDmarcReport} into append-only
 * {@link DmarcAggregateEvidence} snapshots and stores them. It is invoked
 * explicitly (tests, INT-006 {@code DmarcIngestTriggerService}); it is not an
 * HTTP endpoint, not a scheduler, and is not called from INT-003 receive or
 * INT-004 parse. Store itself does not trigger DIAG-001.</p>
 *
 * <p>Grouping: registered Domain × {@code source_ip} × UTC calendar date of
 * {@code date_range.begin}. Homogeneous records on that key persist one row;
 * {@code count} is summed because it is the RFC 7489 row message count on an
 * already-identical fact set. Conflicting authentication/identifier facts on
 * the same key are refused (the freeze does not define a merge). Duplicate
 * {@code report_id} for the same Domain returns the existing snapshots and
 * persists nothing new.</p>
 */
public class DmarcAggregateEvidenceService {

    private final DmarcAggregateEvidenceRepository evidence;

    public DmarcAggregateEvidenceService(DmarcAggregateEvidenceRepository evidence) {
        this.evidence = evidence;
    }

    /**
     * Stores aggregate snapshots for a usable parse outcome. Unusable outcomes
     * persist nothing.
     */
    public List<DmarcAggregateEvidence> store(DmarcReportParseOutcome outcome) {
        if (outcome == null || !outcome.usable()) {
            return List.of();
        }
        return store(outcome.parsed().orElseThrow());
    }

    /**
     * Aggregates and persists snapshots for {@code parsed}. If this Domain +
     * {@code report_id} is already occupied, returns those snapshots unchanged.
     */
    public List<DmarcAggregateEvidence> store(ParsedDmarcReport parsed) {
        return storeResult(parsed).snapshots();
    }

    /**
     * Same as {@link #store(ParsedDmarcReport)} plus whether this call created
     * a new occupancy. INT-006 uses {@link DmarcAggregateStoreResult#createdNew()}
     * so a concurrent losing store does not trigger DIAG-001.
     */
    public DmarcAggregateStoreResult storeResult(ParsedDmarcReport parsed) {
        if (parsed == null) {
            throw new IllegalArgumentException("A parsed DMARC report is required.");
        }
        List<DmarcAggregateEvidence> snapshots = validatedSnapshots(parsed);
        if (snapshots.isEmpty()) {
            return new DmarcAggregateStoreResult(List.of(), false);
        }
        try {
            return evidence.persistNewSnapshotSet(snapshots);
        } catch (DuplicateDmarcAggregateOccupancyException ex) {
            List<DmarcAggregateEvidence> existing = waitForExisting(ex.domainId(), ex.reportId());
            if (existing.isEmpty()) {
                throw ex;
            }
            return new DmarcAggregateStoreResult(existing, false);
        }
    }

    /**
     * Occupancy PK hit means a winner is committing the same Domain +
     * {@code report_id}. Evidence may not be visible until that transaction
     * commits; retry with bounded exponential backoff rather than treating
     * the gap as a permanent failure.
     */
    static final long OCCUPANCY_WAIT_DEADLINE_MS = 2_000L;
    static final long OCCUPANCY_WAIT_INITIAL_MS = 20L;
    static final long OCCUPANCY_WAIT_MAX_SLEEP_MS = 200L;

    private List<DmarcAggregateEvidence> waitForExisting(String domainId, String reportId) {
        long deadline = System.nanoTime() + OCCUPANCY_WAIT_DEADLINE_MS * 1_000_000L;
        long sleepMs = OCCUPANCY_WAIT_INITIAL_MS;
        List<DmarcAggregateEvidence> existing = evidence.findByDomainIdAndReportId(domainId, reportId);
        while (existing.isEmpty() && System.nanoTime() < deadline) {
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
            existing = evidence.findByDomainIdAndReportId(domainId, reportId);
            sleepMs = Math.min(sleepMs * 2L, OCCUPANCY_WAIT_MAX_SLEEP_MS);
        }
        return existing;
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<DmarcAggregateEvidence> findById(String id) {
        return evidence.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<DmarcAggregateEvidence> findByDomainId(String domainId) {
        return evidence.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<DmarcAggregateEvidence> findByOrganizationId(String organizationId) {
        return evidence.findByOrganizationId(organizationId);
    }

    /**
     * Groups and validates every {@code source_ip} set before any persist.
     * Conflicting facts on any group refuse the whole report (zero rows).
     */
    private static List<DmarcAggregateEvidence> validatedSnapshots(ParsedDmarcReport parsed) {
        LocalDate utcDay = utcDay(parsed);
        List<DmarcAggregateEvidence> snapshots = new ArrayList<>();
        for (List<ParsedDmarcRecord> group : groupBySourceIp(parsed.records()).values()) {
            ParsedDmarcRecord facts = homogeneousFacts(group);
            snapshots.add(DmarcAggregateEvidence.fromParsed(
                parsed,
                facts.sourceIp(),
                utcDay,
                summedCount(group),
                facts.spfResult(),
                facts.dkimResult(),
                facts.spfAligned(),
                facts.dkimAligned(),
                facts.envelopeFrom(),
                facts.headerFrom(),
                facts.dkimDomains(),
                facts.disposition()));
        }
        return snapshots;
    }

    private static LocalDate utcDay(ParsedDmarcReport parsed) {
        if (parsed.dateRangeBegin() == null) {
            return null;
        }
        return parsed.dateRangeBegin().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static Map<String, List<ParsedDmarcRecord>> groupBySourceIp(List<ParsedDmarcRecord> records) {
        Map<String, List<ParsedDmarcRecord>> grouped = new LinkedHashMap<>();
        for (ParsedDmarcRecord record : records) {
            grouped.computeIfAbsent(sourceIpKey(record.sourceIp()), ignored -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    private static String sourceIpKey(String sourceIp) {
        return sourceIp == null ? "" : sourceIp;
    }

    /**
     * All records on one grouping key must carry identical non-count facts.
     * The freeze does not define last-wins or concatenation of
     * {@code spf_result}/{@code dkim_result}.
     */
    private static ParsedDmarcRecord homogeneousFacts(List<ParsedDmarcRecord> group) {
        ParsedDmarcRecord first = group.get(0);
        for (int i = 1; i < group.size(); i++) {
            ParsedDmarcRecord next = group.get(i);
            if (!sameNonCountFacts(first, next)) {
                throw new IllegalStateException(
                    "INT-005 freeze does not define how to combine conflicting authentication "
                        + "results for the same source_ip and UTC day.");
            }
        }
        return first;
    }

    private static boolean sameNonCountFacts(ParsedDmarcRecord left, ParsedDmarcRecord right) {
        return Objects.equals(left.sourceIp(), right.sourceIp())
            && Objects.equals(left.spfResult(), right.spfResult())
            && Objects.equals(left.dkimResult(), right.dkimResult())
            && Objects.equals(left.spfAligned(), right.spfAligned())
            && Objects.equals(left.dkimAligned(), right.dkimAligned())
            && Objects.equals(left.envelopeFrom(), right.envelopeFrom())
            && Objects.equals(left.headerFrom(), right.headerFrom())
            && Objects.equals(left.dkimDomains(), right.dkimDomains())
            && Objects.equals(left.disposition(), right.disposition());
    }

    private static Long summedCount(List<ParsedDmarcRecord> group) {
        boolean any = false;
        long sum = 0L;
        for (ParsedDmarcRecord record : group) {
            if (record.count() != null) {
                any = true;
                sum += record.count();
            }
        }
        return any ? sum : null;
    }
}
