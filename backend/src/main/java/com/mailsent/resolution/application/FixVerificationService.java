package com.mailsent.resolution.application;

import com.mailsent.explanation.domain.Explanation;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.domain.HealthSignal;

public class FixVerificationService {

    private final ObservationEvidenceService evidence;
    private final ReputationObservationService reputation;
    private final HealthSignalService healthSignalService;

    public FixVerificationService(ObservationEvidenceService evidence,
                                  ReputationObservationService reputation,
                                  HealthSignalService healthSignalService) {
        this.evidence = evidence;
        this.reputation = reputation;
        this.healthSignalService = healthSignalService;
    }

    public Explanation verifyFix(String domainId, String domainName) {
        if (domainId == null || domainName == null) {
            throw new IllegalArgumentException("Domain ID and Name are required");
        }

        try {
            evidence.record(domainName);
        } catch (Exception ignored) {
            // Best effort verification
        }

        try {
            reputation.record(domainName);
        } catch (Exception ignored) {
            // Best effort verification
        }

        HealthSignal newSignal = healthSignalService.derive(domainName)
            .orElseThrow(() -> new IllegalStateException("Failed to derive health signal"));
        return Explanation.translate(newSignal);
    }
}
