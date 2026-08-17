package com.mailsent.diagnosis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.diagnosis.domain.Alignment;
import com.mailsent.diagnosis.infrastructure.Diag001AlignmentDerivationAdapter;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcIngestDiag001Lineage;
import com.mailsent.portfolio.observation.application.DmarcIngestTriggerService;
import com.mailsent.portfolio.observation.application.DmarcReportParseService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.Rfc7489DmarcAggregateXmlParser;

/**
 * Unit tests for DIAG-001 Analyzer-class Alignment derivation from DMARC
 * Aggregate Evidence (INT-005), invoked through the INT-006 port.
 */
class AlignmentDerivationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");
    private static final LocalDate DAY = LocalDate.of(2021, 1, 1);

    private InMemoryDmarcAggregateEvidenceRepository aggregate;
    private InMemoryObservationEvidenceRepository observationEvidence;
    private AlignmentDerivationService derivation;

    @BeforeEach
    void setUp() {
        aggregate = new InMemoryDmarcAggregateEvidenceRepository();
        observationEvidence = new InMemoryObservationEvidenceRepository();
        derivation = new AlignmentDerivationService(aggregate);
    }

    @Test
    void validDmarcEvidenceDerivesAlignment() {
        DmarcAggregateEvidence snapshot = save(both("agg-1", "192.0.2.1"));

        List<Alignment> derived = derivation.derive(lineage(snapshot));

        assertEquals(1, derived.size());
        Alignment alignment = derived.get(0);
        assertEquals("fail", alignment.spfResult());
        assertEquals("pass", alignment.dkimResult());
        assertEquals("fail", alignment.spfAligned());
        assertEquals("pass", alignment.dkimAligned());
        assertEquals("none", alignment.disposition());
        assertEquals(derived, derivation.lastDerived());
    }

    @Test
    void spfOnlyEvidenceLeavesDkimUnknown() {
        DmarcAggregateEvidence snapshot = save(DmarcAggregateEvidence.restore(
            "agg-spf", "org-1", "dom-1", "example.com", "rec-1", "rid-spf", "192.0.2.1",
            DAY, 1L, "pass", null, "pass", null, "bounce.example.com", "example.com",
            List.of(), "none"));

        Alignment alignment = derivation.derive(lineage(snapshot)).get(0);

        assertEquals("pass", alignment.spfResult());
        assertEquals("pass", alignment.spfAligned());
        assertNull(alignment.dkimResult());
        assertNull(alignment.dkimAligned());
        assertTrue(alignment.dkimDomains().isEmpty());
    }

    @Test
    void dkimOnlyEvidenceLeavesSpfUnknown() {
        DmarcAggregateEvidence snapshot = save(DmarcAggregateEvidence.restore(
            "agg-dkim", "org-1", "dom-1", "example.com", "rec-1", "rid-dkim", "192.0.2.1",
            DAY, 1L, null, "pass", null, "pass", null, "example.com",
            List.of("sel.example.com"), "none"));

        Alignment alignment = derivation.derive(lineage(snapshot)).get(0);

        assertEquals("pass", alignment.dkimResult());
        assertEquals("pass", alignment.dkimAligned());
        assertEquals(List.of("sel.example.com"), alignment.dkimDomains());
        assertNull(alignment.spfResult());
        assertNull(alignment.spfAligned());
        assertNull(alignment.envelopeFrom());
    }

    @Test
    void bothSpfAndDkimArePresentedSeparately() {
        Alignment alignment = derivation.derive(lineage(save(both("agg-both", "192.0.2.1")))).get(0);

        assertEquals("fail", alignment.spfResult());
        assertEquals("pass", alignment.dkimResult());
        assertEquals("fail", alignment.spfAligned());
        assertEquals("pass", alignment.dkimAligned());
        assertNotEquals(alignment.spfResult(), alignment.dkimResult());
        assertNotEquals(alignment.spfAligned(), alignment.dkimAligned());
    }

    @Test
    void missingAuthFactsRemainUnknown() {
        DmarcAggregateEvidence snapshot = save(DmarcAggregateEvidence.restore(
            "agg-missing", "org-1", "dom-1", "example.com", "rec-1", "rid-missing", "192.0.2.1",
            DAY, 1L, null, null, null, null, null, null, List.of(), null));

        Alignment alignment = derivation.derive(lineage(snapshot)).get(0);

        assertNull(alignment.spfResult());
        assertNull(alignment.dkimResult());
        assertNull(alignment.spfAligned());
        assertNull(alignment.dkimAligned());
        assertNull(alignment.envelopeFrom());
        assertNull(alignment.headerFrom());
        assertNull(alignment.disposition());
        assertTrue(alignment.dkimDomains().isEmpty());
    }

    @Test
    void envelopeAndHeaderIdentitiesArePreserved() {
        DmarcAggregateEvidence snapshot = save(both("agg-id", "192.0.2.1"));

        Alignment alignment = derivation.derive(lineage(snapshot)).get(0);

        assertEquals("bounce.mail.example", alignment.envelopeFrom());
        assertEquals("example.com", alignment.headerFrom());
        assertEquals(List.of("sel.example.com"), alignment.dkimDomains());
    }

    @Test
    void multipleEvidenceRowsRetainSeparateLineage() {
        DmarcAggregateEvidence first = save(both("agg-a", "192.0.2.1"));
        DmarcAggregateEvidence second = save(DmarcAggregateEvidence.restore(
            "agg-b", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.2",
            DAY, 9L, "pass", "fail", "pass", "fail", "other.bounce", "example.com",
            List.of("other.example.com"), "quarantine"));
        DmarcIngestDiag001Lineage lineage = new DmarcIngestDiag001Lineage(
            "rec-1", "rid-1", "dom-1", "org-1", List.of(first.id(), second.id()));

        List<Alignment> derived = derivation.derive(lineage);

        assertEquals(2, derived.size());
        assertEquals("agg-a", derived.get(0).evidenceId());
        assertEquals("fail", derived.get(0).spfResult());
        assertEquals("agg-b", derived.get(1).evidenceId());
        assertEquals("pass", derived.get(1).spfResult());
        assertEquals("quarantine", derived.get(1).disposition());
        assertEquals(List.of("agg-a", "agg-b"), derived.stream().map(Alignment::evidenceId).toList());
    }

    @Test
    void tenantIdentityComesFromReceiptAndDomainNotXmlPolicyDomain() {
        DmarcAggregateEvidence snapshot = save(both("agg-tenant", "192.0.2.1"));

        Alignment alignment = derivation.derive(lineage(snapshot)).get(0);

        assertEquals("org-1", alignment.organizationId());
        assertEquals("dom-1", alignment.domainId());
        assertEquals("example.com", alignment.domainName());
        assertNotEquals("other.example", alignment.domainName());
        assertNotEquals("other.example", alignment.organizationId());
    }

    @Test
    void doesNotMutateObservationEvidence() {
        DmarcAggregateEvidence snapshot = save(both("agg-oe", "192.0.2.1"));

        derivation.derive(lineage(snapshot));

        assertTrue(observationEvidence.findByDomainId("dom-1").isEmpty());
        Set<String> fields = Arrays.stream(ObservationEvidence.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("id", "organizationId", "domainId", "domainName", "observedAt",
            "mxRecords", "txtRecords", "spfRecord"), fields);
        assertTrue(Arrays.stream(AlignmentDerivationService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("ObservationEvidence")));
    }

    @Test
    void doesNotPersistAlignment() {
        assertTrue(Arrays.stream(Alignment.class.getDeclaredAnnotations())
            .noneMatch(a -> a.annotationType().getSimpleName().equals("Entity")
                || a.annotationType().getSimpleName().equals("Table")));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.diagnosis.infrastructure.persistence.AlignmentJpaEntity"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.portfolio.observation.infrastructure.persistence.AlignmentJpaEntity"));
        assertTrue(Arrays.stream(AlignmentDerivationService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("EntityManager")
                || f.getType().getSimpleName().contains("Jdbc")
                || f.getName().toLowerCase().contains("repository")));
    }

    @Test
    void doesNotCreateHealthSignal() {
        derivation.derive(lineage(save(both("agg-hs", "192.0.2.1"))));

        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.diagnosis.domain.HealthSignal"));
        assertTrue(Arrays.stream(AlignmentDerivationService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Health")));
    }

    @Test
    void doesNotImplementDiag003() {
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.diagnosis.application.RootCauseService"));
        assertTrue(Arrays.stream(AlignmentDerivationService.class.getDeclaredMethods())
            .map(Method::getName)
            .noneMatch(n -> n.toLowerCase().contains("sender") || n.toLowerCase().contains("rootcause")
                || n.toLowerCase().contains("issue")));
    }

    @Test
    void int006InvokesRealDiag001ThroughExistingPort() {
        InMemoryDomainRepository domains = new InMemoryDomainRepository();
        InMemoryDmarcReportReceiptRepository receipts = new InMemoryDmarcReportReceiptRepository();
        Domain domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
        DmarcReportReceiveService receive = new DmarcReportReceiveService(receipts, domains);
        DmarcReportParseService parse = new DmarcReportParseService(receipts, new Rfc7489DmarcAggregateXmlParser());
        DmarcAggregateEvidenceService store = new DmarcAggregateEvidenceService(aggregate);
        Diag001AlignmentDerivationAdapter adapter = new Diag001AlignmentDerivationAdapter(derivation);
        DmarcIngestTriggerService trigger = new DmarcIngestTriggerService(parse, store, aggregate, adapter);
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 3L)), "rid-port", 1609459200L)
                .getBytes(StandardCharsets.UTF_8)).orElseThrow();

        trigger.process(receipt.id());

        assertEquals(1, derivation.lastDerived().size());
        Alignment alignment = derivation.lastDerived().get(0);
        assertEquals(receipt.id(), alignment.receiptId());
        assertEquals("rid-port", alignment.reportId());
        assertEquals(domain.id(), alignment.domainId());
        assertEquals("org-1", alignment.organizationId());
        assertEquals("fail", alignment.spfAligned());
        assertEquals("pass", alignment.dkimAligned());
        assertInstanceOf(Diag001AlignmentDerivationAdapter.class, adapter);
    }

    @Test
    void duplicateReportDomainRemainsIdempotent() {
        InMemoryDomainRepository domains = new InMemoryDomainRepository();
        InMemoryDmarcReportReceiptRepository receipts = new InMemoryDmarcReportReceiptRepository();
        Domain domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
        DmarcReportReceiveService receive = new DmarcReportReceiveService(receipts, domains);
        DmarcReportParseService parse = new DmarcReportParseService(receipts, new Rfc7489DmarcAggregateXmlParser());
        DmarcAggregateEvidenceService store = new DmarcAggregateEvidenceService(aggregate);
        DmarcIngestTriggerService trigger = new DmarcIngestTriggerService(
            parse, store, aggregate, new Diag001AlignmentDerivationAdapter(derivation));
        byte[] payload = xml(List.of(record("192.0.2.1", 1L)), "same-rid", 1609459200L)
            .getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt first = receive.receive(domain.id(), payload).orElseThrow();
        trigger.process(first.id());
        List<Alignment> afterFirst = derivation.lastDerived();
        DmarcReportReceipt second = receive.receive(domain.id(), payload).orElseThrow();

        trigger.process(second.id());

        assertEquals(1, store.findByDomainId(domain.id()).size());
        assertEquals(1, afterFirst.size());
        assertEquals(afterFirst, derivation.lastDerived());
        List<Alignment> derivedAgain = derivation.derive(new DmarcIngestDiag001Lineage(
            first.id(), "same-rid", domain.id(), "org-1",
            store.findByDomainId(domain.id()).stream().map(DmarcAggregateEvidence::id).toList()));
        assertEquals(afterFirst.get(0).evidenceId(), derivedAgain.get(0).evidenceId());
        assertEquals(afterFirst.get(0).spfAligned(), derivedAgain.get(0).spfAligned());
    }

    @Test
    void lineageContainsReceiptReportDomainOrgAndEvidenceIds() {
        DmarcAggregateEvidence first = save(both("agg-lin-a", "192.0.2.1"));
        DmarcAggregateEvidence second = save(DmarcAggregateEvidence.restore(
            "agg-lin-b", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.2",
            DAY, 1L, "pass", "pass", "pass", "pass", "bounce.mail.example", "example.com",
            List.of("sel.example.com"), "none"));

        List<Alignment> derived = derivation.derive(new DmarcIngestDiag001Lineage(
            "rec-1", "rid-1", "dom-1", "org-1", List.of(first.id(), second.id())));

        for (Alignment alignment : derived) {
            assertEquals("rec-1", alignment.receiptId());
            assertEquals("rid-1", alignment.reportId());
            assertEquals("dom-1", alignment.domainId());
            assertEquals("org-1", alignment.organizationId());
            assertTrue(Set.of("agg-lin-a", "agg-lin-b").contains(alignment.evidenceId()));
        }
        assertEquals(Set.of("agg-lin-a", "agg-lin-b"),
            derived.stream().map(Alignment::evidenceId).collect(Collectors.toSet()));
        assertTrue(Arrays.stream(Alignment.class.getRecordComponents())
            .map(c -> c.getName())
            .noneMatch(n -> n.toLowerCase().contains("observationevidence")));
    }

    @Test
    void derivationIsNotAnHttpEndpointOrScheduler() {
        assertTrue(Arrays.stream(AlignmentDerivationService.class.getDeclaredAnnotations())
            .noneMatch(a -> a.annotationType().getSimpleName().contains("Controller")
                || a.annotationType().getSimpleName().contains("RequestMapping")
                || a.annotationType().getSimpleName().contains("Scheduled")));
        assertTrue(Arrays.stream(AlignmentDerivationService.class.getDeclaredMethods())
            .noneMatch(m -> Arrays.stream(m.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType().getSimpleName().contains("Scheduled")
                    || a.annotationType().getSimpleName().contains("Mapping"))));
    }

    private DmarcAggregateEvidence save(DmarcAggregateEvidence snapshot) {
        return aggregate.save(snapshot);
    }

    private static DmarcAggregateEvidence both(String id, String sourceIp) {
        return DmarcAggregateEvidence.restore(
            id, "org-1", "dom-1", "example.com", "rec-1", "rid-1", sourceIp,
            DAY, 3L, "fail", "pass", "fail", "pass",
            "bounce.mail.example", "example.com", List.of("sel.example.com"), "none");
    }

    private static DmarcIngestDiag001Lineage lineage(DmarcAggregateEvidence snapshot) {
        return new DmarcIngestDiag001Lineage(
            snapshot.receiptId(), snapshot.reportId(), snapshot.domainId(),
            snapshot.organizationId(), List.of(snapshot.id()));
    }

    private static String xml(List<String> records, String reportId, long beginEpochSeconds) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <feedback>
              <report_metadata>
                <org_name>google.com</org_name>
                <report_id>%s</report_id>
                <date_range>
                  <begin>%d</begin>
                  <end>%d</end>
                </date_range>
              </report_metadata>
              <policy_published>
                <domain>other.example</domain>
                <p>quarantine</p>
              </policy_published>
              %s
            </feedback>
            """.formatted(reportId, beginEpochSeconds, beginEpochSeconds + 86399, String.join("\n", records));
    }

    private static String record(String sourceIp, long count) {
        return """
            <record>
              <row>
                <source_ip>%s</source_ip>
                <count>%d</count>
                <policy_evaluated>
                  <disposition>none</disposition>
                  <dkim>pass</dkim>
                  <spf>fail</spf>
                </policy_evaluated>
              </row>
              <identifiers>
                <envelope_from>bounce.mail.example</envelope_from>
                <header_from>example.com</header_from>
              </identifiers>
              <auth_results>
                <dkim>
                  <domain>sel.example.com</domain>
                  <result>pass</result>
                </dkim>
                <spf>
                  <domain>example.com</domain>
                  <result>fail</result>
                </spf>
              </auth_results>
            </record>
            """.formatted(sourceIp, count);
    }
}
