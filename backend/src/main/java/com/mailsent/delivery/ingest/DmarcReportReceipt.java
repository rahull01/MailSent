package com.mailsent.delivery.ingest;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * DmarcReportReceipt — Raw inbound RUA report receipt (INT-003).
 */
public class DmarcReportReceipt {

    public enum IngestStatus {
        RECEIVED,
        INVALID_DOMAIN,
        PARSED,
        FAILED
    }

    private final String id;
    private final String domainId;
    private final String organizationId;
    private final String filename;
    private final byte[] rawPayloadBytes;
    private IngestStatus status;
    private final Instant receivedAt;

    private DmarcReportReceipt(String id, String domainId, String organizationId,
                               String filename, byte[] rawPayloadBytes,
                               IngestStatus status, Instant receivedAt) {
        this.id = Objects.requireNonNull(id);
        this.domainId = Objects.requireNonNull(domainId);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.filename = Objects.requireNonNull(filename);
        this.rawPayloadBytes = Objects.requireNonNull(rawPayloadBytes);
        this.status = Objects.requireNonNull(status);
        this.receivedAt = Objects.requireNonNull(receivedAt);
    }

    public static DmarcReportReceipt receive(String domainId, String organizationId,
                                             String filename, byte[] rawBytes,
                                             Instant receivedAt) {
        if (domainId == null || domainId.isBlank()) {
            throw new IllegalArgumentException("Unknown/unregistered domain uuid: receipt rejected (INV-09).");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must be present for known domain.");
        }
        if (rawBytes == null || rawBytes.length == 0) {
            throw new IllegalArgumentException("Payload bytes must not be empty.");
        }

        return new DmarcReportReceipt(
                UUID.randomUUID().toString(),
                domainId,
                organizationId,
                filename != null ? filename : "report.xml",
                rawBytes,
                IngestStatus.RECEIVED,
                receivedAt != null ? receivedAt : Instant.now()
        );
    }

    public void markParsed() {
        this.status = IngestStatus.PARSED;
    }

    public void markFailed() {
        this.status = IngestStatus.FAILED;
    }

    public String id() { return id; }
    public String domainId() { return domainId; }
    public String organizationId() { return organizationId; }
    public String filename() { return filename; }
    public byte[] rawPayloadBytes() { return rawPayloadBytes; }
    public IngestStatus status() { return status; }
    public Instant receivedAt() { return receivedAt; }
}
