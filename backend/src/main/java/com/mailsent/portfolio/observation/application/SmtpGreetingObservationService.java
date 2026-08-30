package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;
import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;

/**
 * SmtpGreetingObservationService — the INF-006 application boundary.
 *
 * <p>Records immutable SMTP greeting/EHLO snapshots for IPs of observed MX hosts
 * of a registered {@link Domain}. The write use case accepts only a domain name;
 * organization and domain identity are derived from that Domain. MX hostnames
 * are taken from the latest Observation Evidence for the domain (by
 * {@code observedAt}); they are never invented. For each MX hostname, A and
 * AAAA are resolved independently via {@link ForwardAddressLookup}, then SMTP
 * is probed at each IP. A snapshot is persisted only when the probe connected
 * (including a successful empty greeting and/or EHLO). Unavailable A/AAAA skips
 * that family; refused, timed-out, or unavailable SMTP persists nothing for
 * that IP and does not fabricate banner or EHLO text.</p>
 *
 * <p>If there is no Observation Evidence, or the latest snapshot has no MX
 * records, there is no MX to observe: persist nothing (an empty list). That
 * empty successful outcome is not a failure.</p>
 */
public class SmtpGreetingObservationService {

    private final SmtpGreetingObservationRepository observations;
    private final DomainRepository domains;
    private final ObservationEvidenceRepository evidence;
    private final ForwardAddressLookup forward;
    private final SmtpProbe smtp;
    private final com.mailsent.identity.consent.application.ConsentService consentService;

    public SmtpGreetingObservationService(SmtpGreetingObservationRepository observations, DomainRepository domains,
                                          ObservationEvidenceRepository evidence, ForwardAddressLookup forward,
                                          SmtpProbe smtp, com.mailsent.identity.consent.application.ConsentService consentService) {
        this.observations = observations;
        this.domains = domains;
        this.evidence = evidence;
        this.forward = forward;
        this.smtp = smtp;
        this.consentService = consentService;
    }

    /**
     * Observes SMTP greeting/EHLO for a registered domain from its latest
     * Observation Evidence MX hosts and persists every successful connection
     * snapshot.
     *
     * @param domainName the domain to observe (canonicalized)
     * @return the persisted snapshots (possibly empty — no MX to observe, or no
     *         successful SMTP connection)
     * @throws DomainNotRegisteredException if the domain is not registered
     */
    public List<SmtpGreetingObservation> record(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));

        Optional<ObservationEvidence> latest = latestEvidence(domain.id());
        if (latest.isEmpty() || latest.get().mxRecords().isEmpty()) {
            return List.of();
        }

        try {
            consentService.requireConsent(domain.organizationId(), domain.id(), com.mailsent.identity.consent.domain.ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        } catch (com.mailsent.identity.consent.application.ConsentRequiredException e) {
            return List.of();
        }

        Instant observedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        List<SmtpGreetingObservation> persisted = new ArrayList<>();
        for (MxRecord mx : latest.get().mxRecords()) {
            observeFamily(persisted, domain, mx.exchange(), IpAddressFamily.IPV4, observedAt);
            observeFamily(persisted, domain, mx.exchange(), IpAddressFamily.IPV6, observedAt);
        }
        return List.copyOf(persisted);
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<SmtpGreetingObservation> findById(String id) {
        return observations.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<SmtpGreetingObservation> findByDomainId(String domainId) {
        return observations.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<SmtpGreetingObservation> findByOrganizationId(String organizationId) {
        return observations.findByOrganizationId(organizationId);
    }

    private Optional<ObservationEvidence> latestEvidence(String domainId) {
        return evidence.findByDomainId(domainId).stream()
            .max(Comparator.comparing(ObservationEvidence::observedAt)
                .thenComparing(ObservationEvidence::id));
    }

    private void observeFamily(List<SmtpGreetingObservation> persisted, Domain domain, String mxHostname,
                               IpAddressFamily family, Instant observedAt) {
        Optional<List<String>> addresses = lookupQuietly(() -> forward.lookup(mxHostname, family));
        if (addresses.isEmpty()) {
            return;
        }
        for (String ip : addresses.get()) {
            SmtpProbeResult result = probeQuietly(ip, domain.name());
            if (result == null || result.outcome() != SmtpProbeOutcome.CONNECTED) {
                continue;
            }
            persisted.add(observations.save(SmtpGreetingObservation.capture(
                domain, mxHostname, ip, family, result.outcome(), result.rawGreeting(), result.rawEhlo(),
                observedAt)));
        }
    }

    private SmtpProbeResult probeQuietly(String ip, String ehloDomain) {
        try {
            SmtpProbeResult result = smtp.probe(ip, ehloDomain);
            return result == null ? SmtpProbeResult.unavailable() : result;
        } catch (RuntimeException ex) {
            return SmtpProbeResult.unavailable();
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
