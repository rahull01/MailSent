package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;

/**
 * DmarcReportReceiveService — the INT-003 application boundary.
 *
 * <p>Accepts opaque RUA aggregate report bytes addressed to a registered Domain
 * id ({@code uuid} in {@code reports+{uuid}@mailsent.io}). Organization and
 * domain identity are derived from that Domain; the caller does not supply
 * tenant. Unknown or unregistered uuid is discarded: nothing is persisted,
 * no domain is invented, Observation Evidence is not written, Alignment and
 * Health Signal are not created, and DIAG-001 is not triggered (INT-006).</p>
 *
 * <p>INT-003 does not parse XML, does not create Parsed DMARC Report or DMARC
 * Aggregate Evidence, and does not fill {@code report_id}. Duplicate
 * {@code report_id} + Domain idempotency is INT-004/005.</p>
 */
public class DmarcReportReceiveService {

    private final DmarcReportReceiptRepository receipts;
    private final DomainRepository domains;

    public DmarcReportReceiveService(DmarcReportReceiptRepository receipts, DomainRepository domains) {
        this.receipts = receipts;
        this.domains = domains;
    }

    /**
     * Receives opaque RUA bytes for a registered Domain id.
     *
     * @param domainId the RUA uuid (registered Domain id)
     * @param payload  raw attachment bytes as received; {@code null} is stored as empty
     * @return the persisted receipt, or empty when the uuid is unknown
     */
    public Optional<DmarcReportReceipt> receive(String domainId, byte[] payload) {
        if (domainId == null || domainId.isBlank()) {
            return Optional.empty();
        }
        Optional<Domain> registered = domains.findById(domainId);
        if (registered.isEmpty()) {
            return Optional.empty();
        }
        byte[] bytes = payload == null ? new byte[0] : payload;
        Instant receivedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return Optional.of(receipts.save(DmarcReportReceipt.receive(registered.get(), bytes, receivedAt)));
    }

    /** Loads a receipt by its stable identity. */
    public Optional<DmarcReportReceipt> findById(String id) {
        return receipts.findById(id);
    }

    /** Lists receipts for one registered domain. */
    public List<DmarcReportReceipt> findByDomainId(String domainId) {
        return receipts.findByDomainId(domainId);
    }

    /** Lists receipts owned by one organization. */
    public List<DmarcReportReceipt> findByOrganizationId(String organizationId) {
        return receipts.findByOrganizationId(organizationId);
    }
}
