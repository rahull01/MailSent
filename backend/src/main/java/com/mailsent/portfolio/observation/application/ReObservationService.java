package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;

/**
 * ReObservationService — the INF-004 application boundary.
 *
 * <p>Re-observes every registered domain by delegating to
 * {@link ObservationEvidenceService#record} (DNS) then
 * {@link ReputationObservationService#record} (Postmaster/SNDS). This service
 * holds no persistence of its own, never fabricates a result, and never
 * produces a Health Signal (HEALTH-001). A per-domain DNS or registration
 * failure does not abort the remaining domains.</p>
 */
public class ReObservationService {

    private final DomainRepository domains;
    private final ObservationEvidenceService evidence;
    private final ReputationObservationService reputation;
    private final com.mailsent.identity.consent.application.ConsentService consentService;

    public ReObservationService(DomainRepository domains, ObservationEvidenceService evidence,
                                ReputationObservationService reputation,
                                com.mailsent.identity.consent.application.ConsentService consentService) {
        this.domains = domains;
        this.evidence = evidence;
        this.reputation = reputation;
        this.consentService = consentService;
    }

    /** Re-observes DNS then reputation for every registered domain. */
    public void reobserveAll() {
        for (Domain domain : domains.findAll()) {
            try {
                consentService.requireConsent(domain.organizationId(), domain.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
            } catch (com.mailsent.identity.consent.application.ConsentRequiredException e) {
                continue; // skip this domain if consent is missing or revoked
            }
            
            String name = domain.name();
            try {
                evidence.record(name);
            } catch (DnsObservationException | DomainNotRegisteredException ignored) {
                // continue to reputation for this domain, then to the next domain
            }
            try {
                reputation.record(name);
            } catch (DomainNotRegisteredException ignored) {
                // continue to the next domain
            }
        }
    }
}
