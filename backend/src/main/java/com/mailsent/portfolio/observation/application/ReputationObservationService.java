package com.mailsent.portfolio.observation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;

/**
 * ReputationObservationService — the INF-003 application boundary.
 *
 * <p>Records immutable snapshots of registered-domain reputation observations
 * from Google Postmaster and Microsoft SNDS. The write use case accepts only a
 * domain name; organization and domain identity are derived from the registered
 * {@link Domain}. Each provider is queried independently. Persistence happens
 * only for a real observation; unavailability, emptiness, or provider failure
 * stores nothing for that provider (INV-01).</p>
 */
public class ReputationObservationService {

    private final ReputationObservationRepository observations;
    private final DomainRepository domains;
    private final GooglePostmasterPort postmaster;
    private final MicrosoftSndsPort snds;

    public ReputationObservationService(ReputationObservationRepository observations, DomainRepository domains,
                                        GooglePostmasterPort postmaster, MicrosoftSndsPort snds) {
        this.observations = observations;
        this.domains = domains;
        this.postmaster = postmaster;
        this.snds = snds;
    }

    /**
     * Queries both reputation providers for a registered domain and persists
     * every real observation returned.
     *
     * @param domainName the domain to observe (canonicalized)
     * @return the persisted snapshots (possibly empty)
     * @throws DomainNotRegisteredException if the domain is not registered
     */
    public List<ReputationObservation> record(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));

        List<ReputationObservation> persisted = new ArrayList<>();
        queryQuietly(postmaster::query, canonical).ifPresent(observation ->
            persisted.add(observations.save(ReputationObservation.capture(
                domain, ReputationSource.POSTMASTER, observation.payload(), observation.observedAt()))));
        queryQuietly(snds::query, canonical).ifPresent(observation ->
            persisted.add(observations.save(ReputationObservation.capture(
                domain, ReputationSource.SNDS, observation.payload(), observation.observedAt()))));
        return List.copyOf(persisted);
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<ReputationObservation> findById(String id) {
        return observations.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<ReputationObservation> findByDomainId(String domainId) {
        return observations.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<ReputationObservation> findByOrganizationId(String organizationId) {
        return observations.findByOrganizationId(organizationId);
    }

    private Optional<ReputationProviderObservation> queryQuietly(
            Function<String, Optional<ReputationProviderObservation>> port, String domainName) {
        try {
            Optional<ReputationProviderObservation> result = port.apply(domainName);
            return result == null ? Optional.empty() : result;
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
