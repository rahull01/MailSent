package com.mailsent.explanation.application;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mailsent.explanation.domain.Explanation;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.domain.HealthSignal;

@Service
public class ExplanationService {

    private final HealthSignalService healthSignalService;

    public ExplanationService(HealthSignalService healthSignalService) {
        this.healthSignalService = healthSignalService;
    }

    /**
     * Retrieves the latest health signal for a domain and explains it.
     */
    public Optional<Explanation> explainLatest(String domainId) {
        return healthSignalService.findByDomainId(domainId).stream()
            .max(java.util.Comparator.comparing(HealthSignal::observedAt))
            .map(Explanation::translate);
    }
}
