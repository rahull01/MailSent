package com.mailsent.portfolio.observation.application;

import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DnsHealthFacts;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;

/**
 * DnsHealthObservationService — the INF-007 application boundary.
 *
 * <p>Records one immutable snapshot of a registered domain's DNS health
 * observation (TTL, DNSSEC presence, multi-resolver propagation). The write use
 * case accepts only a domain name; organization and domain identity are derived
 * from the registered {@link Domain}. Persistence happens only after a
 * successful observation — a DNS-health failure stores nothing.</p>
 */
public class DnsHealthObservationService {

    private final DnsHealthObservationRepository observations;
    private final DomainRepository domains;
    private final DnsHealthObserver observer;

    public DnsHealthObservationService(DnsHealthObservationRepository observations, DomainRepository domains,
                                       DnsHealthObserver observer) {
        this.observations = observations;
        this.domains = domains;
        this.observer = observer;
    }

    /**
     * Observes a registered domain and persists the factual result as
     * DnsHealthObservation.
     *
     * @param domainName the domain to observe (canonicalized)
     * @return the persisted snapshot
     * @throws DomainNotRegisteredException if the domain is not registered
     * @throws DnsObservationException      if observation fails (nothing is persisted)
     */
    public DnsHealthObservation record(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));
        DnsHealthFacts facts = observer.observe(canonical);
        return observations.save(DnsHealthObservation.capture(domain, facts, facts.observedAt()));
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<DnsHealthObservation> findById(String id) {
        return observations.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<DnsHealthObservation> findByDomainId(String domainId) {
        return observations.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<DnsHealthObservation> findByOrganizationId(String organizationId) {
        return observations.findByOrganizationId(organizationId);
    }
}
