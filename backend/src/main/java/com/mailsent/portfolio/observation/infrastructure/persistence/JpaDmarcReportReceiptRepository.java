package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JpaDmarcReportReceiptRepository — Spring Data repository over
 * {@link DmarcReportReceiptJpaEntity}. Receipts are identified by stable
 * identity and listed by domain or owning organization. The only mutation
 * besides insert is INT-004 {@link #associateReportId} of nullable report_id.
 * No delete queries are defined.
 */
public interface JpaDmarcReportReceiptRepository extends JpaRepository<DmarcReportReceiptJpaEntity, String> {

    List<DmarcReportReceiptJpaEntity> findByDomainId(String domainId);

    List<DmarcReportReceiptJpaEntity> findByOrganizationId(String organizationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE dmarc_report_receipt SET report_id = :reportId WHERE id = :id AND report_id IS NULL",
           nativeQuery = true)
    int associateReportId(@Param("id") String id, @Param("reportId") String reportId);
}
