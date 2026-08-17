package com.mailsent.portfolio.observation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailsent.portfolio.observation.application.Diag001AlignmentDerivationPort;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcIngestTriggerService;
import com.mailsent.portfolio.observation.application.DmarcReportParseService;

/**
 * Configuration and dependency wiring for DMARC ingest trigger (INT-006).
 *
 * <p>No HTTP endpoint and no scheduler. The INT-003 webhook does not call
 * {@link DmarcIngestTriggerService}. Invoke {@code process(receiptId)}
 * explicitly after receive. INF-004 is not extended. Observation Evidence,
 * Alignment persistence, Health Signal, DIAG-002/003, and MAP-001–004 are out
 * of scope. DIAG-001 is invoked through {@link Diag001AlignmentDerivationPort}
 * (wired by Diagnosis).</p>
 */
@Configuration
public class DmarcIngestTriggerConfig {

    @Bean
    DmarcIngestTriggerService dmarcIngestTriggerService(DmarcReportParseService parse,
                                                        DmarcAggregateEvidenceService store,
                                                        DmarcAggregateEvidenceRepository evidence,
                                                        Diag001AlignmentDerivationPort diag001) {
        return new DmarcIngestTriggerService(parse, store, evidence, diag001);
    }
}
