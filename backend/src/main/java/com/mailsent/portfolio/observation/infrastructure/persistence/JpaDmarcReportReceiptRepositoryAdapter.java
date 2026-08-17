package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.observation.application.DmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;

/**
 * JpaDmarcReportReceiptRepositoryAdapter — production
 * {@link DmarcReportReceiptRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests. Payload remains
 * BYTEA; report_id is stored as nullable TEXT.
 */
@Repository
@Profile("!test")
public class JpaDmarcReportReceiptRepositoryAdapter implements DmarcReportReceiptRepository {

    private final JpaDmarcReportReceiptRepository jpa;

    public JpaDmarcReportReceiptRepositoryAdapter(JpaDmarcReportReceiptRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public DmarcReportReceipt save(DmarcReportReceipt receipt) {
        return toDomain(jpa.saveAndFlush(toEntity(receipt)));
    }

    @Override
    @Transactional
    public Optional<DmarcReportReceipt> associateReportId(String receiptId, String reportId) {
        Optional<DmarcReportReceipt> existing = findById(receiptId);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        if (reportId == null || reportId.isBlank() || existing.get().reportId() != null) {
            return existing;
        }
        jpa.associateReportId(receiptId, reportId.trim());
        return findById(receiptId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DmarcReportReceipt> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DmarcReportReceipt> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DmarcReportReceipt> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private DmarcReportReceiptJpaEntity toEntity(DmarcReportReceipt receipt) {
        return new DmarcReportReceiptJpaEntity(
            receipt.id(),
            receipt.organizationId(),
            receipt.domainId(),
            receipt.domainName(),
            receipt.reportId(),
            receipt.payload(),
            receipt.receivedAt());
    }

    private DmarcReportReceipt toDomain(DmarcReportReceiptJpaEntity entity) {
        return DmarcReportReceipt.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            entity.reportId(),
            entity.payload(),
            entity.receivedAt());
    }
}
