package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailsent.portfolio.observation.application.DmarcAggregateXmlParser;
import com.mailsent.portfolio.observation.application.DmarcIngestLimits;
import com.mailsent.portfolio.observation.application.DmarcReportParseService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.Rfc7489DmarcAggregateXmlParser;

/**
 * Configuration and dependency wiring for DMARC Report Parse (INT-004).
 *
 * <p>No HTTP endpoint, no scheduler, and no wiring into INT-003 receive.
 * Parsed facts are in-memory; there is no parsed-report table. INF-004 is
 * not extended. Observation Evidence, DMARC Aggregate Evidence, Alignment,
 * Health Signal, and DIAG-001 are out of scope.</p>
 */
@Configuration
public class DmarcReportParseConfig {

    @Bean
    DmarcAggregateXmlParser dmarcAggregateXmlParser(DmarcIngestLimits limits) {
        return new Rfc7489DmarcAggregateXmlParser(limits);
    }

    @Bean
    DmarcReportParseService dmarcReportParseService(DmarcReportReceiptRepository receipts,
                                                    DmarcAggregateXmlParser parser) {
        return new DmarcReportParseService(receipts, parser);
    }
}
