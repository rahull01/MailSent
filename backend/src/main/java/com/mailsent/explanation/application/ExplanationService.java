package com.mailsent.explanation.application;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mailsent.explanation.domain.Explanation;
import com.mailsent.explanation.domain.ExplanationMode;
import com.mailsent.explanation.domain.ExplanationView;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.domain.HealthSignal;

@Service
public class ExplanationService {

    private final HealthSignalService healthSignalService;
    private final DomainRepository domainRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public ExplanationService(HealthSignalService healthSignalService, DomainRepository domainRepository) {
        this.healthSignalService = Objects.requireNonNull(healthSignalService, "healthSignalService cannot be null");
        this.domainRepository = domainRepository; // optional for backward compatibility in legacy constructor tests
    }

    public ExplanationService(HealthSignalService healthSignalService) {
        this(healthSignalService, null);
    }

    /**
     * Retrieves the latest health signal for a domain and explains it.
     */
    public Optional<Explanation> explainLatest(String domainId) {
        return healthSignalService.findByDomainId(domainId).stream()
            .max(java.util.Comparator.comparing(HealthSignal::observedAt))
            .map(Explanation::translate);
    }

    /**
     * Retrieves the latest health signal for a domain and explains it in the requested ExplanationMode with INV-09 tenant isolation.
     */
    public Optional<ExplanationView> explainLatestWithMode(String organizationId, String domainId, ExplanationMode mode) {
        Objects.requireNonNull(organizationId, "organizationId cannot be null");
        Objects.requireNonNull(domainId, "domainId cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");

        if (domainRepository != null) {
            Domain domain = domainRepository.findById(domainId)
                    .orElseThrow(() -> new IllegalArgumentException("Domain not registered: " + domainId));
            if (!domain.organizationId().equals(organizationId)) {
                throw new IllegalArgumentException("Tenant boundary violation: organizationId does not own domain");
            }
        }

        return explainLatest(domainId)
                .map(explanation -> {
                    if (!explanation.organizationId().equals(organizationId)) {
                        throw new IllegalArgumentException("Tenant boundary violation: organizationId does not match signal");
                    }
                    return explanation.renderView(mode);
                });
    }
}
