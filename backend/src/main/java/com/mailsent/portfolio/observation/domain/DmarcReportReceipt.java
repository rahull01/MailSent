package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * DmarcReportReceipt — an immutable persisted record of opaque RUA aggregate
 * report bytes received for a registered Domain (INT-003).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not Observation
 * Evidence, not Alignment, not a Parsed DMARC Report, and not DMARC Aggregate
 * Evidence. Identity and tenant fields are taken from the registered
 * {@link Domain} (INV-09). The payload is stored as received; INT-003 must not
 * parse XML.</p>
 *
 * <p>{@code report_id} is nullable until INT-004 parse fills it. INT-003 never
 * peeks XML to populate it. Duplicate {@code report_id} + Domain idempotency
 * is INT-004/005 once parse succeeds — this type does not invent content-hash
 * uniqueness. Two posts of the same bytes before parse may yield two receipts.
 * A receipt without {@code report_id} is not a valid stored parsed/aggregate
 * report.</p>
 *
 * <p>Factory-based creation only ({@link #receive}, {@link #restore},
 * {@link #withAssociatedReportId}). No setters and no delete. Payload and
 * identity never change. INT-004 may fill nullable {@code report_id} on the
 * existing receipt after a successful parse (lineage), without a second
 * receipts table.</p>
 */
public final class DmarcReportReceipt {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final String reportId;
    private final byte[] payload;
    private final Instant receivedAt;

    private DmarcReportReceipt(String id, String organizationId, String domainId, String domainName,
                               String reportId, byte[] payload, Instant receivedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.reportId = reportId;
        this.payload = payload == null ? null : Arrays.copyOf(payload, payload.length);
        this.receivedAt = receivedAt == null ? null : receivedAt.truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * Records opaque bytes addressed to a registered Domain. {@code report_id}
     * is always null at receive time (INT-003 does not parse XML).
     */
    public static DmarcReportReceipt receive(Domain domain, byte[] payload, Instant receivedAt) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Received payload bytes are required.");
        }
        if (receivedAt == null) {
            throw new IllegalArgumentException("A receive instant is required.");
        }
        return new DmarcReportReceipt(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            null,
            payload,
            receivedAt);
    }

    /**
     * Reconstructs a receipt from durable storage, adopting stored identity,
     * tenant binding, nullable report_id, payload, and receive instant verbatim.
     */
    public static DmarcReportReceipt restore(String id, String organizationId, String domainId, String domainName,
                                             String reportId, byte[] payload, Instant receivedAt) {
        return new DmarcReportReceipt(id, organizationId, domainId, domainName, reportId, payload, receivedAt);
    }

    /** The stable identity of this receipt (lineage). */
    public String id() {
        return id;
    }

    /** The owning organization, derived from the registered Domain. */
    public String organizationId() {
        return organizationId;
    }

    /** The registered Domain's stable identity (RUA uuid). */
    public String domainId() {
        return domainId;
    }

    /** The registered Domain's canonical name. */
    public String domainName() {
        return domainName;
    }

    /**
     * RFC 7489 {@code report_id} when available after INT-004 parse; always
     * {@code null} for INT-003 receive. Not sufficient to treat this receipt as
     * a Parsed DMARC Report or DMARC Aggregate Evidence.
     */
    public String reportId() {
        return reportId;
    }

    /**
     * INT-004 lineage fill: returns a receipt with the same identity and
     * payload and with {@code report_id} set when it was still null. Does not
     * overwrite a previously associated {@code report_id} and does not change
     * bytes, tenant, or received-at.
     */
    public DmarcReportReceipt withAssociatedReportId(String associatedReportId) {
        if (associatedReportId == null || associatedReportId.isBlank()) {
            throw new IllegalArgumentException("report_id is required to associate.");
        }
        if (this.reportId != null) {
            return this;
        }
        return new DmarcReportReceipt(
            id, organizationId, domainId, domainName, associatedReportId.trim(), payload, receivedAt);
    }

    /** Opaque bytes as received (xml/zip/gz). Never parsed here. */
    public byte[] payload() {
        return payload == null ? null : Arrays.copyOf(payload, payload.length);
    }

    /** The instant this payload was received. */
    public Instant receivedAt() {
        return receivedAt;
    }
}
