package com.mailsent.portfolio.observation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.BlacklistObservation;
import com.mailsent.portfolio.observation.domain.BlacklistSource;

/**
 * BlacklistObservationService — the INF-008 application boundary.
 *
 * <p>Records immutable snapshots of registered-domain blacklist observations
 * from Spamhaus, Barracuda, SURBL, and URIBL. The write use case accepts only a
 * domain name; organization and domain identity are derived from the registered
 * {@link Domain}. Each provider is queried independently. Persistence happens
 * only for a real observation; unavailability, emptiness, or provider failure
 * stores nothing for that provider (INV-01).</p>
 */
public class BlacklistObservationService {

    private final BlacklistObservationRepository observations;
    private final DomainRepository domains;
    private final SpamhausPort spamhaus;
    private final BarracudaPort barracuda;
    private final SurblPort surbl;
    private final UriblPort uribl;

    public BlacklistObservationService(BlacklistObservationRepository observations, DomainRepository domains,
                                       SpamhausPort spamhaus, BarracudaPort barracuda, SurblPort surbl,
                                       UriblPort uribl) {
        this.observations = observations;
        this.domains = domains;
        this.spamhaus = spamhaus;
        this.barracuda = barracuda;
        this.surbl = surbl;
        this.uribl = uribl;
    }

    /**
     * Queries all four blacklist providers for a registered domain and persists
     * every real observation returned.
     *
     * @param domainName the domain to observe (canonicalized)
     * @return the persisted snapshots (possibly empty)
     * @throws DomainNotRegisteredException if the domain is not registered
     */
    public List<BlacklistObservation> record(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));

        List<BlacklistObservation> persisted = new ArrayList<>();
        persistIfPresent(persisted, domain, queryQuietly(spamhaus::query, canonical), BlacklistSource.SPAMHAUS);
        persistIfPresent(persisted, domain, queryQuietly(barracuda::query, canonical), BlacklistSource.BARRACUDA);
        persistIfPresent(persisted, domain, queryQuietly(surbl::query, canonical), BlacklistSource.SURBL);
        persistIfPresent(persisted, domain, queryQuietly(uribl::query, canonical), BlacklistSource.URIBL);
        return List.copyOf(persisted);
    }

    /** Loads a snapshot by its stable identity. */
    public Optional<BlacklistObservation> findById(String id) {
        return observations.findById(id);
    }

    /** Lists snapshots for one registered domain. */
    public List<BlacklistObservation> findByDomainId(String domainId) {
        return observations.findByDomainId(domainId);
    }

    /** Lists snapshots owned by one organization. */
    public List<BlacklistObservation> findByOrganizationId(String organizationId) {
        return observations.findByOrganizationId(organizationId);
    }

    private void persistIfPresent(List<BlacklistObservation> persisted, Domain domain,
                                  Optional<BlacklistProviderObservation> observation, BlacklistSource source) {
        observation.ifPresent(fact ->
            persisted.add(observations.save(BlacklistObservation.capture(
                domain, source, fact.payload(), fact.observedAt()))));
    }

    private Optional<BlacklistProviderObservation> queryQuietly(
            Function<String, Optional<BlacklistProviderObservation>> port, String domainName) {
        try {
            Optional<BlacklistProviderObservation> result = port.apply(domainName);
            return result == null ? Optional.empty() : result;
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
