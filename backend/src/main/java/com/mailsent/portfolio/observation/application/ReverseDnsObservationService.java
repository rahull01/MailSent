package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;

/**
 * ReverseDnsObservationService — the INF-005 application boundary.
 *
 * <p>Records immutable reverse-DNS snapshots for IPs of observed MX hosts of a
 * registered {@link Domain}. The write use case accepts only a domain name;
 * organization and domain identity are derived from that Domain. MX hostnames
 * are taken from the latest Observation Evidence for the domain (by
 * {@code observedAt}); they are never invented. For each MX hostname, A and
 * AAAA are resolved independently, then PTR for each IP. A snapshot is
 * persisted only when PTR lookup succeeded (including a successful empty PTR
 * list). Unavailable A/AAAA skips that family; unavailable PTR persists
 * nothing for that IP.</p>
 *
 * <p>If there is no Observation Evidence, or the latest snapshot has no MX
 * records, there is no MX to observe: persist nothing (an empty list). That
 * empty successful outcome is not a failure.</p>
 */
public class ReverseDnsObservationService {

    private final ReverseDnsObservationRepository observations;
    private final DomainRepository domains;
    private final ObservationEvidenceRepository evidence;
    private final ForwardAddressLookup forward;
    private final ReverseDnsLookup reverse;

    public ReverseDnsObservationService(ReverseDnsObservationRepository observations, DomainRepository domains,
                                        ObservationEvidenceRepository evidence, ForwardAddressLookup forward,
                                        ReverseDnsLookup reverse) {
        this.observations = observations;
        this.domains = domains;
        this.evidence = evidence;
        this.forward = forward;
        this.reverse = reverse;
    }

    /**
     * Observes reverse DNS for a registered domain from its latest Observation
     * Evidence MX hosts and persists every successful PTR snapshot.
     *
     * @param domainName the domain to observe (canonicalized)
     * @return the persisted snapshots (possibly empty — no MX to observe, or no
     *         successful PTR)
     * @throws DomainNotRegisteredException if the domain is not registered
     */
    public List<ReverseDnsObservation> record(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));

        Optional<ObservationEvidence> latest = latestEvidence(domain.id());
        if (latest.isEmpty() || latest.get().mxRecords().isEmpty()) {
            return List.of();
        }

        Instant observedAt = Instant.now();
        List<ReverseDnsObservation> persisted = new ArrayList<>();
        for (MxRecord mx : latest.get().mxRecords()) {
            observeFamily(persisted, domain, mx.exchange(), IpAddressFamily.IPV4, observedAt);
            observeFamily(persisted, domain, mx.exchange(), IpAddressFamily.IPV6, observedAt);
        }
        return List.copyOf(persisted);
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<ReverseDnsObservation> findById(String id) {
        return observations.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<ReverseDnsObservation> findByDomainId(String domainId) {
        return observations.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<ReverseDnsObservation> findByOrganizationId(String organizationId) {
        return observations.findByOrganizationId(organizationId);
    }

    private Optional<ObservationEvidence> latestEvidence(String domainId) {
        return evidence.findByDomainId(domainId).stream()
            .max(Comparator.comparing(ObservationEvidence::observedAt)
                .thenComparing(ObservationEvidence::id));
    }

    private void observeFamily(List<ReverseDnsObservation> persisted, Domain domain, String mxHostname,
                               IpAddressFamily family, Instant observedAt) {
        Optional<List<String>> addresses = lookupQuietly(() -> forward.lookup(mxHostname, family));
        if (addresses.isEmpty()) {
            return;
        }
        for (String ip : addresses.get()) {
            Optional<List<String>> ptrs = lookupQuietly(() -> reverse.lookupPtr(ip));
            ptrs.ifPresent(values ->
                persisted.add(observations.save(ReverseDnsObservation.capture(
                    domain, mxHostname, ip, family, values, observedAt))));
        }
    }

    private <T> Optional<T> lookupQuietly(java.util.function.Supplier<Optional<T>> lookup) {
        try {
            Optional<T> result = lookup.get();
            return result == null ? Optional.empty() : result;
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
