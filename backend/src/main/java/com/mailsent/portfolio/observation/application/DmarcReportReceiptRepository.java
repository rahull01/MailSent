package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;

/**
 * DmarcReportReceiptRepository — the port for persisting and retrieving DMARC
 * Report Receipts (INT-003).
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. The
 * durable PostgreSQL adapter is wired in the non-test profile; a hermetic
 * in-memory adapter backs the tests. Receipts are insert-only except for the
 * narrow INT-004 {@link #associateReportId} lineage fill of nullable
 * {@code report_id}. There is no delete. Repeated receipts of the same domain
 * are additional rows. Duplicate {@code report_id} uniqueness waits until
 * INT-005 when aggregate evidence is stored.</p>
 */
public interface DmarcReportReceiptRepository {

    /** Persists a new receipt and returns it. */
    DmarcReportReceipt save(DmarcReportReceipt receipt);

    /**
     * Fills {@code report_id} on an existing receipt after a successful INT-004
     * parse. No-op when the receipt is missing, {@code report_id} is blank, or
     * the column is already set. Does not rewrite payload or identity.
     */
    Optional<DmarcReportReceipt> associateReportId(String receiptId, String reportId);

    /** Loads a receipt by its stable identity, if present. */
    Optional<DmarcReportReceipt> findById(String id);

    /** Lists all receipts captured for a single registered domain. */
    List<DmarcReportReceipt> findByDomainId(String domainId);

    /** Lists all receipts belonging to a single organization. */
    List<DmarcReportReceipt> findByOrganizationId(String organizationId);
}
