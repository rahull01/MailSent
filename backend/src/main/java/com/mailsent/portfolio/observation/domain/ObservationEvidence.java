package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * ObservationEvidence — an immutable persisted snapshot of one real infrastructure
 * observation (INF-002).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not a new aggregate root:
 * it records what {@link DnsObservation} actually observed for a registered {@link Domain}
 * at one instant. Tenant binding is derived from that Domain (organization and domain
 * identity never come from the caller). {@code observedAt} is taken from the observation
 * itself — never from a second clock.</p>
 *
 * <p>Factory-based creation only ({@link #capture}, {@link #restore}). No setters, no
 * update, no delete. A later observation of the same domain is a new snapshot.</p>
 */
public final class ObservationEvidence {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final Instant observedAt;
    private final List<MxRecord> mxRecords;
    private final List<String> txtRecords;
    private final String spfRecord;

    private ObservationEvidence(String id, String organizationId, String domainId, String domainName,
                                Instant observedAt, List<MxRecord> mxRecords, List<String> txtRecords,
                                String spfRecord) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.observedAt = observedAt;
        this.mxRecords = List.copyOf(mxRecords);
        this.txtRecords = List.copyOf(txtRecords);
        this.spfRecord = spfRecord;
    }

    /**
     * Captures one factual DNS observation against the registered domain that was observed.
     * Identity and tenant fields are taken from {@code domain}; MX, TXT, SPF, and
     * {@code observedAt} are taken from {@code dns}.
     */
    public static ObservationEvidence capture(Domain domain, DnsObservation dns) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (dns == null) {
            throw new IllegalArgumentException("A DNS observation is required.");
        }
        return new ObservationEvidence(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            dns.observedAt(),
            dns.mxRecords(),
            dns.txtRecords(),
            dns.spfRecord().orElse(null));
    }

    /**
     * Reconstructs ObservationEvidence from durable storage, adopting the stored identity,
     * tenant binding, records, and observation instant verbatim.
     */
    public static ObservationEvidence restore(String id, String organizationId, String domainId,
                                              String domainName, Instant observedAt,
                                              List<MxRecord> mxRecords, List<String> txtRecords,
                                              String spfRecord) {
        return new ObservationEvidence(id, organizationId, domainId, domainName, observedAt,
            mxRecords, txtRecords, spfRecord);
    }

    /** The stable identity of this snapshot. */
    public String id() {
        return id;
    }

    /** The owning organization, derived from the registered Domain. */
    public String organizationId() {
        return organizationId;
    }

    /** The registered Domain's stable identity. */
    public String domainId() {
        return domainId;
    }

    /** The registered Domain's canonical name. */
    public String domainName() {
        return domainName;
    }

    /** The instant the DNS observation was made (from {@link DnsObservation}). */
    public Instant observedAt() {
        return observedAt;
    }

    /** The observed MX records, as captured from {@link DnsObservation}. */
    public List<MxRecord> mxRecords() {
        return mxRecords;
    }

    /** The observed TXT strings, as captured from {@link DnsObservation}. */
    public List<String> txtRecords() {
        return txtRecords;
    }

    /** The observed SPF record, if present. */
    public Optional<String> spfRecord() {
        return Optional.ofNullable(spfRecord);
    }
}
