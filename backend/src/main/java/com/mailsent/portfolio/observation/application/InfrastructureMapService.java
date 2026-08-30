package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.BlacklistObservation;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReputationObservation;

/**
 * InfrastructureMapService — MAP-001/MAP-002 application topology read-model.
 *
 * <p>Builds the frozen WF-5 chain Domain → DNS → SPF → DKIM → DMARC → Senders →
 * Inbox for one registered Domain. Observation Evidence is the primary source
 * for DNS and SPF. DKIM, DMARC, Senders, and Inbox are always
 * {@link NodeAvailability#NOT_OBSERVED} (no DKIM/DMARC snapshot, no DIAG-002,
 * Inbox is a glossary destination). Reputation, DNS Health, and Blacklist
 * snapshots may appear only as optional evidence references on DNS — never as
 * extra node types.</p>
 *
 * <p>MAP-002 consumes the canonical HEALTH-001 {@link HealthSignal} to project
 * verified health indications on nodes without altering raw observation availability
 * or multi-tenant boundaries (INV-09).</p>
 */
public class InfrastructureMapService {

    private final DomainRepository domains;
    private final ObservationEvidenceService evidence;
    private final ReputationObservationService reputation;
    private final DnsHealthObservationService dnsHealth;
    private final BlacklistObservationService blacklist;
    private final HealthSignalService healthSignalService;

    public InfrastructureMapService(DomainRepository domains, ObservationEvidenceService evidence,
                                    ReputationObservationService reputation,
                                    DnsHealthObservationService dnsHealth,
                                    BlacklistObservationService blacklist,
                                    HealthSignalService healthSignalService) {
        this.domains = domains;
        this.evidence = evidence;
        this.reputation = reputation;
        this.dnsHealth = dnsHealth;
        this.blacklist = blacklist;
        this.healthSignalService = healthSignalService;
    }

    public InfrastructureMapService(DomainRepository domains, ObservationEvidenceService evidence,
                                    ReputationObservationService reputation,
                                    DnsHealthObservationService dnsHealth,
                                    BlacklistObservationService blacklist) {
        this(domains, evidence, reputation, dnsHealth, blacklist, null);
    }

    /**
     * Builds the topology for a registered domain.
     *
     * @param domainName the domain to map (canonicalized)
     * @return the in-memory topology read-model
     * @throws DomainNotRegisteredException if the domain is not registered
     */
    public InfrastructureMap forDomain(String domainName) {
        String canonical = Domain.normalize(domainName);
        Domain domain = domains.findByName(canonical)
            .orElseThrow(() -> new DomainNotRegisteredException(canonical));
        Optional<ObservationEvidence> latestEvidence = latestOwned(
            evidence.findByDomainId(domain.id()),
            ObservationEvidence::organizationId,
            ObservationEvidence::observedAt,
            ObservationEvidence::id,
            domain.organizationId());

        HealthIndication domainHealth = HealthIndication.UNKNOWN;
        if (healthSignalService != null) {
            Optional<HealthSignal> latestSignal = latestOwned(
                healthSignalService.findByDomainId(domain.id()),
                HealthSignal::organizationId,
                HealthSignal::observedAt,
                HealthSignal::id,
                domain.organizationId());
            if (latestSignal.isPresent()) {
                domainHealth = latestSignal.get().indication();
            }
        }

        List<InfrastructureMapNode> nodes = List.of(
            node(domain, InfrastructureNodeType.DOMAIN, NodeAvailability.OBSERVED, domainHealth),
            node(domain, InfrastructureNodeType.DNS,
                latestEvidence.isPresent() ? NodeAvailability.OBSERVED : NodeAvailability.UNAVAILABLE,
                domainHealth),
            node(domain, InfrastructureNodeType.SPF, spfAvailability(latestEvidence),
                latestEvidence.isPresent() && latestEvidence.get().spfRecord().isPresent() ? domainHealth : HealthIndication.UNKNOWN),
            node(domain, InfrastructureNodeType.DKIM, NodeAvailability.NOT_OBSERVED, domainHealth),
            node(domain, InfrastructureNodeType.DMARC, NodeAvailability.NOT_OBSERVED, domainHealth),
            node(domain, InfrastructureNodeType.SENDERS, NodeAvailability.NOT_OBSERVED, HealthIndication.UNKNOWN),
            node(domain, InfrastructureNodeType.INBOX, NodeAvailability.NOT_OBSERVED, domainHealth)
        );

        List<InfrastructureMapRelationship> relationships = List.of(
            edge(domain, InfrastructureNodeType.DOMAIN, InfrastructureNodeType.DNS,
                InfrastructureRelationshipType.DOMAIN_TO_DNS),
            edge(domain, InfrastructureNodeType.DNS, InfrastructureNodeType.SPF,
                InfrastructureRelationshipType.DNS_TO_SPF),
            edge(domain, InfrastructureNodeType.SPF, InfrastructureNodeType.DKIM,
                InfrastructureRelationshipType.SPF_TO_DKIM),
            edge(domain, InfrastructureNodeType.DKIM, InfrastructureNodeType.DMARC,
                InfrastructureRelationshipType.DKIM_TO_DMARC),
            edge(domain, InfrastructureNodeType.DMARC, InfrastructureNodeType.SENDERS,
                InfrastructureRelationshipType.DMARC_TO_SENDERS),
            edge(domain, InfrastructureNodeType.SENDERS, InfrastructureNodeType.INBOX,
                InfrastructureRelationshipType.SENDERS_TO_INBOX)
        );

        return new InfrastructureMap(
            domain.organizationId(),
            domain.id(),
            domain.name(),
            nodes,
            relationships,
            dnsEvidence(domain, latestEvidence));
    }

    private List<InfrastructureEvidenceRef> dnsEvidence(Domain domain,
                                                        Optional<ObservationEvidence> latestEvidence) {
        if (latestEvidence.isEmpty()) {
            return List.of();
        }
        ObservationEvidence oe = latestEvidence.get();
        List<InfrastructureEvidenceRef> refs = new ArrayList<>();
        refs.add(new InfrastructureEvidenceRef(
            oe.id(), InfrastructureEvidenceKind.OBSERVATION_EVIDENCE, oe.observedAt()));
        latestOwned(
            reputation.findByDomainId(domain.id()),
            ReputationObservation::organizationId,
            ReputationObservation::observedAt,
            ReputationObservation::id,
            domain.organizationId())
            .ifPresent(obs -> refs.add(new InfrastructureEvidenceRef(
                obs.id(), InfrastructureEvidenceKind.REPUTATION_OBSERVATION, obs.observedAt())));
        latestOwned(
            dnsHealth.findByDomainId(domain.id()),
            DnsHealthObservation::organizationId,
            DnsHealthObservation::observedAt,
            DnsHealthObservation::id,
            domain.organizationId())
            .ifPresent(obs -> refs.add(new InfrastructureEvidenceRef(
                obs.id(), InfrastructureEvidenceKind.DNS_HEALTH_OBSERVATION, obs.observedAt())));
        latestOwned(
            blacklist.findByDomainId(domain.id()),
            BlacklistObservation::organizationId,
            BlacklistObservation::observedAt,
            BlacklistObservation::id,
            domain.organizationId())
            .ifPresent(obs -> refs.add(new InfrastructureEvidenceRef(
                obs.id(), InfrastructureEvidenceKind.BLACKLIST_OBSERVATION, obs.observedAt())));
        return refs;
    }

    private static NodeAvailability spfAvailability(Optional<ObservationEvidence> latestEvidence) {
        if (latestEvidence.isEmpty()) {
            return NodeAvailability.UNAVAILABLE;
        }
        return latestEvidence.get().spfRecord().isPresent()
            ? NodeAvailability.OBSERVED
            : NodeAvailability.UNAVAILABLE;
    }

    private static InfrastructureMapNode node(Domain domain, InfrastructureNodeType type,
                                              NodeAvailability availability, HealthIndication health) {
        return new InfrastructureMapNode(identity(domain, type), type, availability, health);
    }

    private static InfrastructureMapRelationship edge(Domain domain, InfrastructureNodeType from,
                                                      InfrastructureNodeType to,
                                                      InfrastructureRelationshipType type) {
        return new InfrastructureMapRelationship(identity(domain, from), identity(domain, to), type);
    }

    private static String identity(Domain domain, InfrastructureNodeType type) {
        return domain.id() + ":" + type.name();
    }

    private static <T> Optional<T> latestOwned(List<T> items, Function<T, String> organizationId,
                                               Function<T, Instant> observedAt, Function<T, String> id,
                                               String ownerOrganizationId) {
        return items.stream()
            .filter(item -> ownerOrganizationId.equals(organizationId.apply(item)))
            .max(Comparator.comparing(observedAt).thenComparing(id));
    }
}
