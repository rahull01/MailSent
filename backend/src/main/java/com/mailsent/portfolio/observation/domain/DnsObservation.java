package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * DnsObservation — the result of observing a domain's DNS records (INF-001).
 *
 * <p>An immutable value object holding only what was actually observed — the domain's MX
 * records, TXT records (including the SPF record, the TXT string starting with
 * {@code v=spf1}), and the observation instant. Records are normalized deterministically
 * (MX sorted by preference, TXT sorted lexicographically) so the value is stable for
 * comparison. It never fabricates records (INV-01 truth); empty lists mean the domain
 * genuinely had no such records.</p>
 */
public final class DnsObservation {

    private final String domainName;
    private final List<MxRecord> mxRecords;
    private final List<String> txtRecords;
    private final String spfRecord;
    private final Instant observedAt;

    private DnsObservation(String domainName, List<MxRecord> mxRecords, List<String> txtRecords,
                           String spfRecord, Instant observedAt) {
        this.domainName = domainName;
        this.mxRecords = List.copyOf(mxRecords);
        this.txtRecords = List.copyOf(txtRecords);
        this.spfRecord = spfRecord;
        this.observedAt = observedAt;
    }

    /**
     * Builds an observation from observed records, normalizing deterministically.
     *
     * @param domainName the observed domain name, required
     * @param mxRecords  the observed MX records, required (may be empty)
     * @param txtRecords the observed TXT strings, required (may be empty)
     * @param spfRecord  the observed SPF TXT string, or {@code null} when absent
     * @param observedAt the observation instant, required
     */
    public static DnsObservation of(String domainName, List<MxRecord> mxRecords, List<String> txtRecords,
                                    String spfRecord, Instant observedAt) {
        if (domainName == null || domainName.isBlank()) {
            throw new IllegalArgumentException("A domain name is required for an observation.");
        }
        if (mxRecords == null || txtRecords == null) {
            throw new IllegalArgumentException("Observation record lists are required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation time is required.");
        }
        List<MxRecord> sortedMx = mxRecords.stream()
            .sorted(Comparator.comparingInt(MxRecord::priority))
            .toList();
        List<String> sortedTxt = txtRecords.stream().sorted().toList();
        return new DnsObservation(domainName, sortedMx, sortedTxt, spfRecord, observedAt);
    }

    /** The observed domain name. */
    public String domainName() {
        return domainName;
    }

    /** The observed MX records, ordered by preference. */
    public List<MxRecord> mxRecords() {
        return mxRecords;
    }

    /** The observed TXT strings, sorted deterministically. */
    public List<String> txtRecords() {
        return txtRecords;
    }

    /** The observed SPF record (the {@code v=spf1} TXT string), if present. */
    public Optional<String> spfRecord() {
        return Optional.ofNullable(spfRecord);
    }

    /** The instant the observation was made. */
    public Instant observedAt() {
        return observedAt;
    }
}