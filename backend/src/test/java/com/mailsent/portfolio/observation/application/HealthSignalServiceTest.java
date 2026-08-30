package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.diagnosis.application.AlignmentDerivationService;
import com.mailsent.diagnosis.domain.Alignment;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryHealthSignalRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryReputationObservationRepository;

/**
 * Unit tests for {@link HealthSignalService} (HEALTH-001) — interpretation,
 * missing sources, lineage, tenant isolation, and idempotency.
 */
class HealthSignalServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");
    private static final LocalDate DAY = LocalDate.of(2021, 1, 1);

    private InMemoryDomainRepository domains;
    private InMemoryObservationEvidenceRepository evidence;
    private InMemoryDmarcAggregateEvidenceRepository aggregate;
    private InMemoryReputationObservationRepository reputation;
    private InMemoryHealthSignalRepository signals;
    private HealthSignalService service;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        aggregate = new InMemoryDmarcAggregateEvidenceRepository();
        reputation = new InMemoryReputationObservationRepository();
        signals = new InMemoryHealthSignalRepository();
        service = new HealthSignalService(
            signals, domains, evidence, aggregate, reputation, Clock.fixed(NOW, ZoneOffset.UTC));
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void missingObservationEvidencePersistsNothing() {
        aggregate.save(healthy("agg-1", domain, "rid-1"));
        reputation.save(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW));

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(signals.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void missingAggregateEvidencePersistsNothing() {
        evidence.save(oe("oe-1", domain, NOW));
        reputation.save(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW));

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(signals.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void missingReputationObservationPersistsNothing() {
        evidence.save(oe("oe-1", domain, NOW));
        aggregate.save(healthy("agg-1", domain, "rid-1"));

        assertTrue(service.record("example.com").isEmpty());
        assertTrue(signals.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void healthyOneRow() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(HealthIndication.HEALTHY, signal.indication());
        assertEquals("healthy", signal.indication().token());
    }

    @Test
    void healthyMultiIp() {
        seedCompleteSources(domain, "oe-1", List.of(
                row("agg-a", domain, "rid-1", "192.0.2.1", "pass", "pass", "pass", "pass", "none"),
                row("agg-b", domain, "rid-1", "192.0.2.2", "pass", "pass", "pass", "pass", "none")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(HealthIndication.HEALTHY, signal.indication());
        assertEquals(List.of("agg-a", "agg-b"), signal.dmarcAggregateEvidenceIds());
    }

    @Test
    void atRiskDispositionQuarantine() {
        assertEquals(HealthIndication.AT_RISK, recordJudgment("pass", "pass", "pass", "pass", "quarantine"));
    }

    @Test
    void atRiskDispositionReject() {
        assertEquals(HealthIndication.AT_RISK, recordJudgment("pass", "pass", "pass", "pass", "reject"));
    }

    @Test
    void atRiskSpfResultFail() {
        assertEquals(HealthIndication.AT_RISK, recordJudgment("fail", "pass", "pass", "pass", "none"));
    }

    @Test
    void atRiskDkimResultFail() {
        assertEquals(HealthIndication.AT_RISK, recordJudgment("pass", "fail", "pass", "pass", "none"));
    }

    @Test
    void atRiskSpfAlignedFail() {
        assertEquals(HealthIndication.AT_RISK, recordJudgment("pass", "pass", "fail", "pass", "none"));
    }

    @Test
    void atRiskDkimAlignedFail() {
        assertEquals(HealthIndication.AT_RISK, recordJudgment("pass", "pass", "pass", "fail", "none"));
    }

    @Test
    void atRiskAnyIpFailWins() {
        seedCompleteSources(domain, "oe-1", List.of(
                row("agg-ok", domain, "rid-1", "192.0.2.1", "pass", "pass", "pass", "pass", "none"),
                row("agg-fail", domain, "rid-1", "192.0.2.2", "fail", "pass", "pass", "pass", "none")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        assertEquals(HealthIndication.AT_RISK, service.record("example.com").orElseThrow().indication());
    }

    @Test
    void unknownWhenJudgmentFieldsNull() {
        DmarcAggregateEvidence partial = DmarcAggregateEvidence.restore(
            "agg-partial", domain.organizationId(), domain.id(), domain.name(), "rec-1", "rid-1",
            "192.0.2.1", DAY, 1L, null, null, null, null, null, null, List.of(), null);
        seedCompleteSources(domain, "oe-1", List.of(partial),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(HealthIndication.UNKNOWN, signal.indication());
        assertEquals(List.of("agg-partial"), signal.dmarcAggregateEvidenceIds());
    }

    @Test
    void unknownWhenSpfPassAndDkimNone() {
        assertEquals(HealthIndication.UNKNOWN, recordJudgment("pass", "none", "pass", "pass", "none"));
    }

    @Test
    void unknownWhenMixedToken() {
        assertEquals(HealthIndication.UNKNOWN, recordJudgment("pass", "mixed", "pass", "pass", "none"));
    }

    @Test
    void unknownWhenNeutral() {
        assertEquals(HealthIndication.UNKNOWN, recordJudgment("neutral", "pass", "pass", "pass", "none"));
    }

    @Test
    void unknownWhenSoftfail() {
        assertEquals(HealthIndication.UNKNOWN, recordJudgment("softfail", "pass", "pass", "pass", "none"));
    }

    @Test
    void unknownWhenTemperror() {
        assertEquals(HealthIndication.UNKNOWN, recordJudgment("temperror", "pass", "pass", "pass", "none"));
    }

    @Test
    void unknownWhenPermerror() {
        assertEquals(HealthIndication.UNKNOWN, recordJudgment("permerror", "pass", "pass", "pass", "none"));
    }

    @Test
    void unknownWhenDispositionPass() {
        HealthIndication indication = recordJudgment("pass", "pass", "pass", "pass", "pass");
        assertEquals(HealthIndication.UNKNOWN, indication);
        assertNotEquals(HealthIndication.HEALTHY, indication);
    }

    @Test
    void caseAndWhitespaceAreNormalized() {
        seedCompleteSources(domain, "oe-1", List.of(
                row("agg-ws", domain, "rid-1", "192.0.2.1", " Pass ", "PASS", " pass", "Pass", " NONE ")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));
        assertEquals(HealthIndication.HEALTHY, service.record("example.com").orElseThrow().indication());

        Domain other = Domain.register("org-1", "risk.example.com", NOW);
        domains.save(other);
        seedCompleteSources(other, "oe-r", List.of(
                row("agg-q", other, "rid-r", "192.0.2.1", "pass", "pass", "pass", "pass", " Quarantine ")),
            List.of(rep("rep-r", other, ReputationSource.POSTMASTER, NOW)));
        assertEquals(HealthIndication.AT_RISK, service.record("risk.example.com").orElseThrow().indication());
    }

    @Test
    void observationEvidenceValuesDoNotVote() {
        ObservationEvidence failingLookingDns = ObservationEvidence.restore(
            "oe-1", domain.organizationId(), domain.id(), domain.name(), NOW,
            List.of(new MxRecord(10, "mail.example.com")), List.of("v=spf1 -all"), "v=spf1 -all");
        evidence.save(failingLookingDns);
        aggregate.save(healthy("agg-1", domain, "rid-1"));
        reputation.save(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW));

        assertEquals(HealthIndication.HEALTHY, service.record("example.com").orElseThrow().indication());
    }

    @Test
    void reputationPayloadDoesNotVote() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(ReputationObservation.restore(
                "rep-1", domain.organizationId(), domain.id(), domain.name(),
                ReputationSource.POSTMASTER, NOW, "FAIL listed bad")));

        assertEquals(HealthIndication.HEALTHY, service.record("example.com").orElseThrow().indication());
    }

    @Test
    void citesAggregateEvidenceNotDiag001LastDerived() {
        DmarcAggregateEvidence stored = row("agg-stored", domain, "rid-1", "192.0.2.1",
            "fail", "fail", "fail", "fail", "none");
        seedCompleteSources(domain, "oe-1", List.of(stored),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        AlignmentDerivationService derivation = new AlignmentDerivationService(aggregate);
        List<Alignment> derived = derivation.derive(new DmarcIngestDiag001Lineage(
            stored.receiptId(), stored.reportId(), stored.domainId(), stored.organizationId(),
            List.of(stored.id())));
        assertEquals(1, derived.size());
        assertEquals(derived, derivation.lastDerived());

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(HealthIndication.AT_RISK, signal.indication());
        assertEquals(List.of("agg-stored"), signal.dmarcAggregateEvidenceIds());
        assertTrue(Arrays.stream(HealthSignalService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().equals(AlignmentDerivationService.class)
                || f.getType().getSimpleName().contains("Alignment")
                || f.getType().getSimpleName().contains("Diag001")));
        assertTrue(Arrays.stream(HealthSignal.class.getDeclaredFields())
            .map(Field::getName)
            .noneMatch(n -> n.toLowerCase().contains("alignment") || n.toLowerCase().contains("diag")));
    }

    @Test
    void tenantIsolationDoesNotCiteAnotherOrganizationsSnapshots() {
        Domain other = Domain.register("org-2", "other.com", NOW);
        domains.save(other);
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));
        seedCompleteSources(other, "oe-other", List.of(row("agg-other", other, "rid-o", "192.0.2.9",
                "fail", "fail", "fail", "fail", "reject")),
            List.of(rep("rep-other", other, ReputationSource.POSTMASTER, NOW)));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals("org-1", signal.organizationId());
        assertEquals(HealthIndication.HEALTHY, signal.indication());
        assertEquals("oe-1", signal.observationEvidenceId());
        assertEquals(List.of("agg-1"), signal.dmarcAggregateEvidenceIds());
        assertEquals(List.of("rep-1"), signal.reputationObservationIds());
        assertEquals(1, service.findByOrganizationId("org-1").size());
        assertTrue(service.findByOrganizationId("org-2").isEmpty());
    }

    @Test
    void lineageCarriesExactCitedIds() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        HealthSignal signal = service.record("example.com").orElseThrow();
        Set<String> fields = Arrays.stream(HealthSignal.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("id", "organizationId", "domainId", "domainName", "indication",
            "observationEvidenceId", "dmarcAggregateEvidenceIds", "reputationObservationIds",
            "observedAt"), fields);
        assertEquals("oe-1", signal.observationEvidenceId());
        assertEquals(List.of("agg-1"), signal.dmarcAggregateEvidenceIds());
        assertEquals(List.of("rep-1"), signal.reputationObservationIds());
        assertEquals(NOW, signal.observedAt());
    }

    @Test
    void sameCitedIdsPersistNothingNew() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        HealthSignal first = service.record("example.com").orElseThrow();
        Optional<HealthSignal> second = service.record("example.com");

        assertTrue(second.isEmpty());
        assertEquals(1, signals.findByDomainId(domain.id()).size());
        assertEquals(first.id(), signals.findById(first.id()).orElseThrow().id());
    }

    @Test
    void newSnapshotPersistsNewSignal() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));
        HealthSignal first = service.record("example.com").orElseThrow();

        evidence.save(oe("oe-2", domain, Instant.parse("2026-08-16T10:00:00Z")));
        HealthSignal second = service.record("example.com").orElseThrow();

        assertNotEquals(first.id(), second.id());
        assertEquals("oe-2", second.observationEvidenceId());
        assertEquals(2, signals.findByDomainId(domain.id()).size());
        assertEquals(HealthIndication.HEALTHY, first.indication());
        assertEquals(HealthIndication.HEALTHY, second.indication());
        assertEquals(HealthIndication.HEALTHY, signals.findById(first.id()).orElseThrow().indication());
    }

    @Test
    void doesNotMutateObservationEvidence() {
        ObservationEvidence original = oe("oe-1", domain, NOW);
        seedCompleteSources(domain, original.id(), List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));
        ObservationEvidence before = evidence.findById("oe-1").orElseThrow();

        service.record("example.com");

        ObservationEvidence after = evidence.findById("oe-1").orElseThrow();
        assertEquals(before.id(), after.id());
        assertEquals(before.mxRecords(), after.mxRecords());
        assertEquals(before.txtRecords(), after.txtRecords());
        assertEquals(before.spfRecord(), after.spfRecord());
        assertEquals(1, evidence.findByDomainId(domain.id()).size());
    }

    @Test
    void doesNotImplementMapTrustScoreOrHealth002() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));
        HealthSignal signal = service.record("example.com").orElseThrow();

        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.diagnosis.application.RootCauseService"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.portfolio.observation.domain.TrustScore"));
        assertTrue(Arrays.stream(HealthSignal.class.getDeclaredFields())
            .map(Field::getName)
            .noneMatch(n -> n.toLowerCase().contains("color") || n.toLowerCase().contains("score")
                || n.toLowerCase().contains("weight") || n.toLowerCase().contains("trend")
                || n.equals("green") || n.equals("yellow") || n.equals("red")));
        assertFalse("green".equals(signal.indication().token())
            || "yellow".equals(signal.indication().token())
            || "red".equals(signal.indication().token()));
        assertTrue(Arrays.stream(HealthSignalService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("InfrastructureMap")));
    }

    @Test
    void deriveIsTheSameWriteUseCaseAsRecord() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        assertTrue(service.record("example.com").isPresent());
        assertTrue(service.derive("example.com").isEmpty(), "same cited ids persist nothing new");
    }

    @Test
    void citesLatestReputationPerSourceWithoutInventingUnavailableProvider() {
        reputation.save(rep("rep-old", domain, ReputationSource.POSTMASTER, Instant.parse("2026-08-01T10:00:00Z")));
        reputation.save(rep("rep-new", domain, ReputationSource.POSTMASTER, Instant.parse("2026-08-14T10:00:00Z")));
        evidence.save(oe("oe-1", domain, NOW));
        aggregate.save(healthy("agg-1", domain, "rid-1"));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(List.of("rep-new"), signal.reputationObservationIds());
    }

    @Test
    void bothReputationProvidersAreCitedWhenBothExist() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(
                rep("rep-g", domain, ReputationSource.POSTMASTER, NOW),
                rep("rep-m", domain, ReputationSource.SNDS, NOW)));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(List.of("rep-g", "rep-m"), signal.reputationObservationIds());
    }

    @Test
    void citesAllRowsOfLatestAggregateReport() {
        aggregate.save(DmarcAggregateEvidence.restore(
            "agg-old-a", domain.organizationId(), domain.id(), domain.name(), "rec-1", "rid-old",
            "192.0.2.1", LocalDate.of(2020, 12, 31), 1L, "pass", "pass", "pass", "pass",
            "bounce.example.com", domain.name(), List.of("sel.example.com"), "none"));
        aggregate.save(row("agg-new-a", domain, "rid-new", "192.0.2.1", "fail", "fail", "fail", "fail", "none"));
        aggregate.save(row("agg-new-b", domain, "rid-new", "192.0.2.2", "pass", "fail", "pass", "fail", "none"));
        evidence.save(oe("oe-1", domain, NOW));
        reputation.save(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals(List.of("agg-new-a", "agg-new-b"), signal.dmarcAggregateEvidenceIds());
        assertFalse(signal.dmarcAggregateEvidenceIds().contains("agg-old-a"));
        assertEquals(HealthIndication.AT_RISK, signal.indication());
    }

    @Test
    void citesLatestObservationEvidenceOnly() {
        evidence.save(oe("oe-old", domain, Instant.parse("2026-08-01T10:00:00Z")));
        evidence.save(oe("oe-new", domain, NOW));
        aggregate.save(healthy("agg-1", domain, "rid-1"));
        reputation.save(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW));

        HealthSignal signal = service.record("example.com").orElseThrow();
        assertEquals("oe-new", signal.observationEvidenceId());
    }

    @Test
    void organizationIdComesFromRegisteredDomain() {
        seedCompleteSources(domain, "oe-1", List.of(healthy("agg-1", domain, "rid-1")),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));

        HealthSignal signal = service.record("  Example.COM  ").orElseThrow();
        assertEquals(domain.organizationId(), signal.organizationId());
        assertEquals(domain.id(), signal.domainId());
        assertEquals(domain.name(), signal.domainName());
        assertEquals(NOW, signal.observedAt());
    }

    @Test
    void unregisteredDomainIsRejectedAndPersistsNothing() {
        assertThrows(DomainNotRegisteredException.class, () -> service.record("not-registered.com"));
        assertTrue(signals.findByOrganizationId("org-1").isEmpty());
    }

    @Test
    void triggerIsRecordByDomainNameOnlyNotHttpOrScheduler() {
        assertEquals(1, Arrays.stream(HealthSignalService.class.getDeclaredMethods())
            .filter(m -> "record".equals(m.getName()))
            .map(Method::getParameterCount)
            .findFirst()
            .orElseThrow());
        assertTrue(Arrays.stream(HealthSignalService.class.getDeclaredAnnotations())
            .noneMatch(a -> a.annotationType().getSimpleName().contains("Controller")
                || a.annotationType().getSimpleName().contains("RequestMapping")
                || a.annotationType().getSimpleName().contains("Scheduled")));
        Set<String> ctorParams = Arrays.stream(HealthSignalService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of(
            "HealthSignalRepository",
            "DomainRepository",
            "ObservationEvidenceRepository",
            "DmarcAggregateEvidenceRepository",
            "ReputationObservationRepository",
            "Clock"), ctorParams);
        assertTrue(Arrays.stream(ReObservationService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().equals(HealthSignalService.class)));
        assertTrue(Arrays.stream(DmarcIngestTriggerService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().equals(HealthSignalService.class)));
    }

    private HealthIndication recordJudgment(String spfResult, String dkimResult, String spfAligned,
                                            String dkimAligned, String disposition) {
        signals = new InMemoryHealthSignalRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        aggregate = new InMemoryDmarcAggregateEvidenceRepository();
        reputation = new InMemoryReputationObservationRepository();
        service = new HealthSignalService(
            signals, domains, evidence, aggregate, reputation, Clock.fixed(NOW, ZoneOffset.UTC));
        seedCompleteSources(domain, "oe-1", List.of(
                row("agg-1", domain, "rid-1", "192.0.2.1", spfResult, dkimResult, spfAligned, dkimAligned, disposition)),
            List.of(rep("rep-1", domain, ReputationSource.POSTMASTER, NOW)));
        return service.record("example.com").orElseThrow().indication();
    }

    private void seedCompleteSources(Domain registered, String evidenceId, List<DmarcAggregateEvidence> aggregates,
                                     List<ReputationObservation> observations) {
        evidence.save(oe(evidenceId, registered, NOW));
        aggregates.forEach(aggregate::save);
        observations.forEach(reputation::save);
    }

    private static ObservationEvidence oe(String id, Domain registered, Instant observedAt) {
        return ObservationEvidence.restore(
            id, registered.organizationId(), registered.id(), registered.name(), observedAt,
            List.of(new MxRecord(10, "mail.example.com")), List.of("v=spf1 -all"), "v=spf1 -all");
    }

    private static DmarcAggregateEvidence healthy(String id, Domain registered, String reportId) {
        return row(id, registered, reportId, "192.0.2.1", "pass", "pass", "pass", "pass", "none");
    }

    private static DmarcAggregateEvidence row(String id, Domain registered, String reportId, String sourceIp,
                                              String spfResult, String dkimResult, String spfAligned,
                                              String dkimAligned, String disposition) {
        return DmarcAggregateEvidence.restore(
            id, registered.organizationId(), registered.id(), registered.name(), "rec-1", reportId,
            sourceIp, DAY, 1L, spfResult, dkimResult, spfAligned, dkimAligned, "bounce.example.com",
            registered.name(), List.of("sel.example.com"), disposition);
    }

    private static ReputationObservation rep(String id, Domain registered, ReputationSource source, Instant observedAt) {
        return ReputationObservation.restore(
            id, registered.organizationId(), registered.id(), registered.name(), source, observedAt, "raw");
    }
}
