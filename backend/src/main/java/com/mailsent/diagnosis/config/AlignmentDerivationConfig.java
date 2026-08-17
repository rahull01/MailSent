package com.mailsent.diagnosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailsent.diagnosis.application.AlignmentDerivationService;
import com.mailsent.diagnosis.infrastructure.Diag001AlignmentDerivationAdapter;
import com.mailsent.portfolio.observation.application.Diag001AlignmentDerivationPort;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;

/**
 * Configuration and dependency wiring for DIAG-001 Alignment derivation.
 *
 * <p>No HTTP endpoint, no scheduler, and no Alignment persistence. The
 * production {@link Diag001AlignmentDerivationPort} is this derivation, not a
 * no-op.</p>
 */
@Configuration
public class AlignmentDerivationConfig {

    @Bean
    AlignmentDerivationService alignmentDerivationService(DmarcAggregateEvidenceRepository evidence) {
        return new AlignmentDerivationService(evidence);
    }

    @Bean
    Diag001AlignmentDerivationPort diag001AlignmentDerivationPort(AlignmentDerivationService derivation) {
        return new Diag001AlignmentDerivationAdapter(derivation);
    }
}
