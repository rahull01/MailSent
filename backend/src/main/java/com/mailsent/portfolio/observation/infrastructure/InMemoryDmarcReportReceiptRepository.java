package com.mailsent.portfolio.observation.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.portfolio.observation.application.DmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;

/**
 * InMemoryDmarcReportReceiptRepository — a hermetic, thread-safe
 * {@link DmarcReportReceiptRepository} for the test profile. Receipts are keyed
 * by their stable identity; lookups by domain or organization scan the store.
 */
public class InMemoryDmarcReportReceiptRepository implements DmarcReportReceiptRepository {

    private final Map<String, DmarcReportReceipt> byId = new ConcurrentHashMap<>();

    @Override
    public DmarcReportReceipt save(DmarcReportReceipt receipt) {
        byId.put(receipt.id(), receipt);
        return receipt;
    }

    @Override
    public Optional<DmarcReportReceipt> associateReportId(String receiptId, String reportId) {
        DmarcReportReceipt existing = byId.get(receiptId);
        if (existing == null) {
            return Optional.empty();
        }
        if (reportId == null || reportId.isBlank() || existing.reportId() != null) {
            return Optional.of(existing);
        }
        DmarcReportReceipt associated = existing.withAssociatedReportId(reportId);
        byId.put(receiptId, associated);
        return Optional.of(associated);
    }

    @Override
    public Optional<DmarcReportReceipt> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<DmarcReportReceipt> findByDomainId(String domainId) {
        return byId.values().stream()
            .filter(receipt -> receipt.domainId().equals(domainId))
            .toList();
    }

    @Override
    public List<DmarcReportReceipt> findByOrganizationId(String organizationId) {
        return byId.values().stream()
            .filter(receipt -> receipt.organizationId().equals(organizationId))
            .toList();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
