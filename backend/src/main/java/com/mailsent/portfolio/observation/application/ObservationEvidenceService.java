package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;

/**
 * ObservationEvidenceService — the INF-002 application boundary.
 *
 * <p>Records one immutable snapshot of a registered domain's DNS observation. The write
 * use case accepts only a domain name; organization and domain identity are derived from
 * the registered {@link Domain}. Observation is delegated to the existing
 * {@link DnsObservationService} (INF-001, unchanged). Persistence happens only after a
 * successful observation — a DNS failure stores nothing.</p>
 */
public class ObservationEvidenceService {

    private final ObservationEvidenceRepository evidence;
    private final DomainRepository domains;
    private final DnsObservationService dnsObservationService;

    public ObservationEvidenceService(ObservationEvidenceRepository evidence, DomainRepository domains,
                                      DnsObservationService dnsObservationService) {
        this.evidence = evidence;
        this.domains = domains;
        this.dnsObservationService = dnsObservationService;
    }

    /**
     * Observes a registered domain and persists the factual result as ObservationEvidence.
     *
     * @param domainName the domain to observe (canonicalized)
     * @return the persisted snapshot
     * @throws DomainNotRegisteredException if the domain is not registered
     * @throws DnsObservationException      if the DNS lookup fails (nothing is persisted)
     */
    public ObservationEvidence record(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));
        DnsObservation dns = dnsObservationService.observe(canonical);
        return evidence.save(ObservationEvidence.capture(domain, dns));
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<ObservationEvidence> findById(String id) {
        return evidence.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<ObservationEvidence> findByDomainId(String domainId) {
        return evidence.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<ObservationEvidence> findByOrganizationId(String organizationId) {
        return evidence.findByOrganizationId(organizationId);
    }
}
