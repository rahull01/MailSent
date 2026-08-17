package com.mailsent.portfolio.observation.application;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DnsObservation;

/**
 * DnsObservationService — the INF-001 application boundary.
 *
 * <p>Observation is gated on the domain being registered in the organization's domain
 * registry (only registered/authorized domains may be observed), then delegated to the
 * {@link DnsResolver} port. The domain name is canonicalized by the existing {@link Domain}
 * contract, and the observation belongs to that single registered domain — it never crosses
 * an organization boundary (INV-09). Results are real observations (INV-01), never
 * fabricated.</p>
 */
public class DnsObservationService {

    private final DnsResolver resolver;
    private final DomainRepository domains;
    private final com.mailsent.identity.consent.application.ConsentService consentService;

    public DnsObservationService(DnsResolver resolver, DomainRepository domains, com.mailsent.identity.consent.application.ConsentService consentService) {
        this.resolver = resolver;
        this.domains = domains;
        this.consentService = consentService;
    }

    /**
     * Observes the DNS records of a registered domain.
     *
     * @param domainName the domain to observe (case-folded canonical form)
     * @return the observed records
     * @throws DomainNotRegisteredException if the domain is not registered
     * @throws DnsObservationException      if the DNS lookup fails
     */
    public DnsObservation observe(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical).orElseThrow(() -> new DomainNotRegisteredException(canonical));
        consentService.requireConsent(domain.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        return resolver.observe(canonical);
    }
}