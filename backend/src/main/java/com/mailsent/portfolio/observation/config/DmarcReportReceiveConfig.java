package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.observation.application.DmarcIngestLimits;
import com.mailsent.portfolio.observation.application.DmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcReportReceiptRepository;

/**
 * Configuration and dependency wiring for DMARC Report Receive (INT-003).
 *
 * <p>There is exactly one {@link DmarcReportReceiptRepository} bean per
 * profile: the PostgreSQL adapter ({@code JpaDmarcReportReceiptRepositoryAdapter})
 * is component-scanned under {@code !test}; the hermetic
 * {@link InMemoryDmarcReportReceiptRepository} is declared here under the
 * {@code test} profile.</p>
 *
 * <p>INT-003 registers an inbound ingest webhook (not a public product API)
 * and no scheduler. INF-004 is not extended. Observation Evidence is not
 * written. Parsed DMARC Report, DMARC Aggregate Evidence, Alignment, Health
 * Signal, and DIAG-001 are out of scope.</p>
 */
@Configuration
public class DmarcReportReceiveConfig {

    @Bean
    DmarcIngestLimits dmarcIngestLimits() {
        return DmarcIngestLimits.production();
    }

    @Bean
    @Profile("test")
    DmarcReportReceiptRepository inMemoryDmarcReportReceiptRepository() {
        return new InMemoryDmarcReportReceiptRepository();
    }

    @Bean
    DmarcReportReceiveService dmarcReportReceiveService(DmarcReportReceiptRepository receipts,
                                                        DomainRepository domains) {
        return new DmarcReportReceiveService(receipts, domains);
    }
}
