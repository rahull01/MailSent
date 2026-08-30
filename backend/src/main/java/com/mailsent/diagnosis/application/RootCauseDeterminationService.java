package com.mailsent.diagnosis.application;

import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to determine the root cause of an at-risk HealthSignal (DIAG-003).
 */
@Service
public class RootCauseDeterminationService {

    private final DmarcAggregateEvidenceRepository evidenceRepository;

    public RootCauseDeterminationService(DmarcAggregateEvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    /**
     * Determines a specific RootCause by examining the lineage of an at-risk HealthSignal.
     */
    public Optional<RootCause> determineRootCause(HealthSignal healthSignal) {
        if (healthSignal == null) {
            throw new IllegalArgumentException("HealthSignal must not be null.");
        }

        if (healthSignal.indication() != HealthIndication.AT_RISK) {
            return Optional.empty();
        }

        List<DmarcAggregateEvidence> evidenceList = healthSignal.dmarcAggregateEvidenceIds().stream()
                .map(evidenceRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        for (DmarcAggregateEvidence evidence : evidenceList) {
            // Prioritize disposition failures
            if ("reject".equalsIgnoreCase(evidence.disposition())) {
                return Optional.of(RootCause.DMARC_REJECT);
            }
            if ("quarantine".equalsIgnoreCase(evidence.disposition())) {
                return Optional.of(RootCause.DMARC_QUARANTINE);
            }
            // Then alignment failures
            if ("fail".equalsIgnoreCase(evidence.spfAligned())) {
                return Optional.of(RootCause.SPF_ALIGNMENT_FAILURE);
            }
            if ("fail".equalsIgnoreCase(evidence.dkimAligned())) {
                return Optional.of(RootCause.DKIM_ALIGNMENT_FAILURE);
            }
            // Then authentication failures
            if ("fail".equalsIgnoreCase(evidence.spfResult())) {
                return Optional.of(RootCause.SPF_AUTHENTICATION_FAILURE);
            }
            if ("fail".equalsIgnoreCase(evidence.dkimResult())) {
                return Optional.of(RootCause.DKIM_AUTHENTICATION_FAILURE);
            }
        }

        return Optional.of(RootCause.UNKNOWN_ISSUE);
    }
}
