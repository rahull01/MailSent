package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.BlacklistObservation;
import com.mailsent.portfolio.observation.domain.BlacklistSource;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;
import com.mailsent.portfolio.observation.infrastructure.InMemoryBlacklistObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDnsHealthObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReputationObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.UnavailableBarracudaAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableGooglePostmasterAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableMicrosoftSndsAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableSpamhausAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableSurblAdapter;
import com.mailsent.portfolio.observation.infrastructure.UnavailableUriblAdapter;

/**
 * Unit tests for {@link InfrastructureMapService} (MAP-001) — topology chain,
 * truthful unobserved nodes, tenant isolation, and no fabricated DKIM/DMARC/
 * senders/inbox facts.
 */
class InfrastructureMapServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-14T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryObservationEvidenceRepository evidenceRepo;
    private InMemoryReputationObservationRepository reputationRepo;
    private InMemoryDnsHealthObservationRepository dnsHealthRepo;
    private InMemoryBlacklistObservationRepository blacklistRepo;
    private InfrastructureMapService service;
    private Domain example;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        evidenceRepo = new InMemoryObservationEvidenceRepository();
        reputationRepo = new InMemoryReputationObservationRepository();
        dnsHealthRepo = new InMemoryDnsHealthObservationRepository();
        blacklistRepo = new InMemoryBlacklistObservationRepository();
        DnsObservationService dns = new DnsObservationService(domainName -> {
            throw new DnsObservationException("MAP-001 must not observe DNS");
        }, domains, org.mockito.Mockito.mock(com.mailsent.identity.consent.application.ConsentService.class));
        ObservationEvidenceService evidence = new ObservationEvidenceService(evidenceRepo, domains, dns);
        ReputationObservationService reputation = new ReputationObservationService(
            reputationRepo, domains, new UnavailableGooglePostmasterAdapter(),
            new UnavailableMicrosoftSndsAdapter());
        DnsHealthObservationService dnsHealth = new DnsHealthObservationService(
            dnsHealthRepo, domains, domainName -> {
                throw new DnsObservationException("MAP-001 must not observe DNS health");
            });
        BlacklistObservationService blacklist = new BlacklistObservationService(
            blacklistRepo, domains, new UnavailableSpamhausAdapter(), new UnavailableBarracudaAdapter(),
            new UnavailableSurblAdapter(), new UnavailableUriblAdapter());
        service = new InfrastructureMapService(domains, evidence, reputation, dnsHealth, blacklist);
        example = domains.save(Domain.register("org-1", "example.com", NOW));
    }

    @Test
    void unregisteredDomainIsRejected() {
        assertThrows(DomainNotRegisteredException.class, () -> service.forDomain("not-registered.com"));
    }

    @Test
    void domainNameIsNormalized() {
        InfrastructureMap map = service.forDomain("  Example.COM  ");
        assertEquals(example.id(), map.domainId());
        assertEquals("example.com", map.domainName());
        assertEquals("org-1", map.organizationId());
    }

    @Test
    void frozenChainHasSevenNodesAndSixRelationships() {
        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(List.of(
            InfrastructureNodeType.DOMAIN,
            InfrastructureNodeType.DNS,
            InfrastructureNodeType.SPF,
            InfrastructureNodeType.DKIM,
            InfrastructureNodeType.DMARC,
            InfrastructureNodeType.SENDERS,
            InfrastructureNodeType.INBOX), map.nodes().stream().map(InfrastructureMapNode::type).toList());
        assertEquals(List.of(
            InfrastructureRelationshipType.DOMAIN_TO_DNS,
            InfrastructureRelationshipType.DNS_TO_SPF,
            InfrastructureRelationshipType.SPF_TO_DKIM,
            InfrastructureRelationshipType.DKIM_TO_DMARC,
            InfrastructureRelationshipType.DMARC_TO_SENDERS,
            InfrastructureRelationshipType.SENDERS_TO_INBOX),
            map.relationships().stream().map(InfrastructureMapRelationship::type).toList());
        assertEquals(node(example, InfrastructureNodeType.DOMAIN), map.relationships().get(0).fromIdentity());
        assertEquals(node(example, InfrastructureNodeType.DNS), map.relationships().get(0).toIdentity());
        assertEquals(node(example, InfrastructureNodeType.INBOX), map.relationships().get(5).toIdentity());
    }

    @Test
    void withoutObservationEvidenceDnsAndSpfAreUnavailable() {
        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.DOMAIN));
        assertEquals(NodeAvailability.UNAVAILABLE, availability(map, InfrastructureNodeType.DNS));
        assertEquals(NodeAvailability.UNAVAILABLE, availability(map, InfrastructureNodeType.SPF));
        assertUnobservedDownstream(map);
        assertTrue(map.dnsEvidence().isEmpty());
    }

    @Test
    void observationEvidenceWithoutSpfObservesDnsAndLeavesSpfUnavailable() {
        saveEvidence(example, null, NOW);

        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.DNS));
        assertEquals(NodeAvailability.UNAVAILABLE, availability(map, InfrastructureNodeType.SPF));
        assertUnobservedDownstream(map);
        assertEquals(1, map.dnsEvidence().size());
        assertEquals(InfrastructureEvidenceKind.OBSERVATION_EVIDENCE, map.dnsEvidence().get(0).kind());
    }

    @Test
    void observationEvidenceWithSpfObservesDnsAndSpf() {
        ObservationEvidence saved = saveEvidence(example, "v=spf1 -all", NOW);

        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.DNS));
        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.SPF));
        assertUnobservedDownstream(map);
        assertEquals(saved.id(), map.dnsEvidence().get(0).snapshotId());
    }

    @Test
    void latestObservationEvidenceWins() {
        saveEvidence(example, null, Instant.parse("2026-08-14T10:00:00Z"));
        ObservationEvidence later = saveEvidence(example, "v=spf1 include:_spf.google.com -all",
            Instant.parse("2026-08-14T12:00:00Z"));

        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.SPF));
        assertEquals(later.id(), map.dnsEvidence().get(0).snapshotId());
    }

    @Test
    void txtThatLooksLikeDkimDoesNotFabricateDkimOrDmarcOrSendersOrInbox() {
        DnsObservation dns = DnsObservation.of("example.com",
            List.of(new MxRecord(10, "mail.example.com")),
            List.of("v=DKIM1; k=rsa; p=abc", "v=DMARC1; p=none"),
            "v=spf1 -all", NOW);
        evidenceRepo.save(ObservationEvidence.capture(example, dns));

        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.SPF));
        assertUnobservedDownstream(map);
    }

    @Test
    void organizationsRemainIsolated() {
        Domain other = domains.save(Domain.register("org-2", "other.com", NOW));
        ObservationEvidence org1Evidence = saveEvidence(example, "v=spf1 -all", NOW);
        saveEvidence(other, "v=spf1 include:org2.example -all", NOW);

        InfrastructureMap map = service.forDomain("example.com");

        assertEquals("org-1", map.organizationId());
        assertEquals(example.id(), map.domainId());
        assertEquals(org1Evidence.id(), map.dnsEvidence().get(0).snapshotId());
        assertFalse(map.dnsEvidence().stream().anyMatch(ref -> !ref.snapshotId().equals(org1Evidence.id())));
        assertEquals(NodeAvailability.OBSERVED, availability(map, InfrastructureNodeType.SPF));

        InfrastructureMap otherMap = service.forDomain("other.com");
        assertEquals("org-2", otherMap.organizationId());
        assertEquals(other.id(), otherMap.domainId());
        assertFalse(otherMap.dnsEvidence().stream().anyMatch(ref -> ref.snapshotId().equals(org1Evidence.id())));
    }

    @Test
    void extraSnapshotsAttachAsDnsEvidenceNotAsNodeTypes() {
        ObservationEvidence oe = saveEvidence(example, "v=spf1 -all", NOW);
        ReputationObservation reputation = reputationRepo.save(
            ReputationObservation.capture(example, ReputationSource.POSTMASTER, "raw", NOW));
        DnsHealthObservation health = dnsHealthRepo.save(
            DnsHealthObservation.restore("dh-1", example.organizationId(), example.id(), example.name(),
                NOW, List.of(), List.of(), List.of()));
        BlacklistObservation listed = blacklistRepo.save(
            BlacklistObservation.capture(example, BlacklistSource.SPAMHAUS, "raw", NOW));

        InfrastructureMap map = service.forDomain("example.com");

        Set<InfrastructureNodeType> types = map.nodes().stream()
            .map(InfrastructureMapNode::type).collect(Collectors.toSet());
        assertEquals(Set.of(
            InfrastructureNodeType.DOMAIN,
            InfrastructureNodeType.DNS,
            InfrastructureNodeType.SPF,
            InfrastructureNodeType.DKIM,
            InfrastructureNodeType.DMARC,
            InfrastructureNodeType.SENDERS,
            InfrastructureNodeType.INBOX), types);
        assertEquals(7, map.nodes().size());
        assertEquals(Set.of(
            oe.id(), reputation.id(), health.id(), listed.id()),
            map.dnsEvidence().stream().map(InfrastructureEvidenceRef::snapshotId).collect(Collectors.toSet()));
        assertEquals(Set.of(
            InfrastructureEvidenceKind.OBSERVATION_EVIDENCE,
            InfrastructureEvidenceKind.REPUTATION_OBSERVATION,
            InfrastructureEvidenceKind.DNS_HEALTH_OBSERVATION,
            InfrastructureEvidenceKind.BLACKLIST_OBSERVATION),
            map.dnsEvidence().stream().map(InfrastructureEvidenceRef::kind).collect(Collectors.toSet()));
    }

    @Test
    void extraSnapshotsWithoutObservationEvidenceAreIgnored() {
        reputationRepo.save(ReputationObservation.capture(example, ReputationSource.SNDS, "raw", NOW));

        InfrastructureMap map = service.forDomain("example.com");

        assertEquals(NodeAvailability.UNAVAILABLE, availability(map, InfrastructureNodeType.DNS));
        assertTrue(map.dnsEvidence().isEmpty());
    }

    @Test
    void readModelHasNoScoreColorHealthOrSeverity() {
        for (Class<?> type : List.of(InfrastructureMap.class, InfrastructureMapNode.class,
            InfrastructureMapRelationship.class, InfrastructureEvidenceRef.class)) {
            Set<String> names = Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
            assertFalse(names.contains("score"));
            assertFalse(names.contains("color"));
            assertFalse(names.contains("health"));
            assertFalse(names.contains("severity"));
        }
        assertEquals(Set.of("identity", "type", "availability"),
            Arrays.stream(InfrastructureMapNode.class.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet()));
    }

    private ObservationEvidence saveEvidence(Domain domain, String spf, Instant observedAt) {
        DnsObservation dns = DnsObservation.of(domain.name(), List.of(), List.of(), spf, observedAt);
        return evidenceRepo.save(ObservationEvidence.capture(domain, dns));
    }

    private static void assertUnobservedDownstream(InfrastructureMap map) {
        assertEquals(NodeAvailability.NOT_OBSERVED, availability(map, InfrastructureNodeType.DKIM));
        assertEquals(NodeAvailability.NOT_OBSERVED, availability(map, InfrastructureNodeType.DMARC));
        assertEquals(NodeAvailability.NOT_OBSERVED, availability(map, InfrastructureNodeType.SENDERS));
        assertEquals(NodeAvailability.NOT_OBSERVED, availability(map, InfrastructureNodeType.INBOX));
    }

    private static NodeAvailability availability(InfrastructureMap map, InfrastructureNodeType type) {
        return map.nodes().stream()
            .filter(node -> node.type() == type)
            .map(InfrastructureMapNode::availability)
            .findFirst()
            .orElseThrow();
    }

    private static String node(Domain domain, InfrastructureNodeType type) {
        return domain.id() + ":" + type.name();
    }
}
