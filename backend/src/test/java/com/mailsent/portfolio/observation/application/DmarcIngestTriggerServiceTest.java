package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.Rfc7489DmarcAggregateXmlParser;
import com.mailsent.portfolio.observation.interfaces.DmarcReportReceiveController;

/**
 * Unit tests for {@link DmarcIngestTriggerService} (INT-006) — DIAG-001 only
 * after a new INT-005 store, with lineage, and no other analysis triggers.
 */
class DmarcIngestTriggerServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryDmarcReportReceiptRepository receipts;
    private InMemoryObservationEvidenceRepository observationEvidence;
    private InMemoryDmarcAggregateEvidenceRepository aggregate;
    private DmarcReportReceiveService receive;
    private DmarcReportParseService parse;
    private DmarcAggregateEvidenceService store;
    private FakeDiag001AlignmentDerivationPort diag001;
    private DmarcIngestTriggerService trigger;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        receipts = new InMemoryDmarcReportReceiptRepository();
        observationEvidence = new InMemoryObservationEvidenceRepository();
        aggregate = new InMemoryDmarcAggregateEvidenceRepository();
        receive = new DmarcReportReceiveService(receipts, domains);
        parse = new DmarcReportParseService(receipts, new Rfc7489DmarcAggregateXmlParser());
        store = new DmarcAggregateEvidenceService(aggregate);
        diag001 = new FakeDiag001AlignmentDerivationPort();
        trigger = new DmarcIngestTriggerService(parse, store, aggregate, diag001);
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void successfulNewAggregateTriggersDiag001OnceWithLineage() {
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 3L), record("192.0.2.2", 1L)), "rid-new", 1609459200L)
                .getBytes(StandardCharsets.UTF_8)).orElseThrow();

        trigger.process(receipt.id());

        assertEquals(1, diag001.invocationCount());
        DmarcIngestDiag001Lineage lineage = diag001.last();
        assertEquals(receipt.id(), lineage.receiptId());
        assertEquals("rid-new", lineage.reportId());
        assertEquals(domain.id(), lineage.domainId());
        assertEquals("org-1", lineage.organizationId());
        List<DmarcAggregateEvidence> stored = store.findByDomainId(domain.id());
        assertEquals(2, stored.size());
        assertEquals(stored.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()),
            Set.copyOf(lineage.evidenceIds()));
        assertTrue(observationEvidence.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void duplicateReportIdAndDomainDoesNotTriggerAgain() {
        byte[] payload = xml(List.of(record("192.0.2.1", 3L)), "same-rid", 1609459200L)
            .getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt first = receive.receive(domain.id(), payload).orElseThrow();
        trigger.process(first.id());
        DmarcReportReceipt second = receive.receive(domain.id(), payload).orElseThrow();

        trigger.process(second.id());

        assertEquals(1, diag001.invocationCount());
        assertEquals(first.id(), diag001.last().receiptId());
        assertEquals(1, store.findByDomainId(domain.id()).size());
    }

    @Test
    void processingTheSameReceiptTwiceDoesNotTriggerAgain() {
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 1L)), "once", 1609459200L).getBytes(StandardCharsets.UTF_8))
            .orElseThrow();

        trigger.process(receipt.id());
        trigger.process(receipt.id());

        assertEquals(1, diag001.invocationCount());
    }

    @Test
    void parseFailureDoesNotTrigger() {
        DmarcReportReceipt receipt = receive.receive(domain.id(), "<not-feedback/>".getBytes(StandardCharsets.UTF_8))
            .orElseThrow();

        trigger.process(receipt.id());

        assertEquals(0, diag001.invocationCount());
        assertTrue(store.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void aggregateStoreFailureDoesNotTrigger() {
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(
                record("192.0.2.1", 1L),
                conflictingRecord("192.0.2.1", 2L)), "rid-conflict", 1609459200L)
                .getBytes(StandardCharsets.UTF_8)).orElseThrow();

        assertThrows(IllegalStateException.class, () -> trigger.process(receipt.id()));
        assertEquals(0, diag001.invocationCount());
        assertTrue(store.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void receiveDoesNotTrigger() {
        receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 1L)), "rid-receive", 1609459200L).getBytes(StandardCharsets.UTF_8));

        assertEquals(0, diag001.invocationCount());
        assertTrue(store.findByDomainId(domain.id()).isEmpty());
        Set<String> ctorParams = Arrays.stream(DmarcReportReceiveService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("DmarcReportReceiptRepository", "DomainRepository"), ctorParams);
        assertTrue(Arrays.stream(DmarcReportReceiveController.class.getDeclaredFields())
            .noneMatch(f -> f.getType().equals(DmarcIngestTriggerService.class)
                || f.getType().equals(Diag001AlignmentDerivationPort.class)));
    }

    @Test
    void parseDoesNotTrigger() {
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 1L)), "rid-parse", 1609459200L).getBytes(StandardCharsets.UTF_8))
            .orElseThrow();

        parse.parse(receipt.id());

        assertEquals(0, diag001.invocationCount());
        assertTrue(store.findByDomainId(domain.id()).isEmpty());
        Set<String> ctorParams = Arrays.stream(DmarcReportParseService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("DmarcReportReceiptRepository", "DmarcAggregateXmlParser"), ctorParams);
    }

    @Test
    void concurrentDuplicateProcessTriggersDiag001Once() throws Exception {
        byte[] payload = xml(List.of(record("192.0.2.1", 1L)), "rid-once", 1609459200L)
            .getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt first = receive.receive(domain.id(), payload).orElseThrow();
        DmarcReportReceipt second = receive.receive(domain.id(), payload).orElseThrow();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            pool.submit(() -> {
                ready.countDown();
                go.await();
                trigger.process(first.id());
                return null;
            });
            pool.submit(() -> {
                ready.countDown();
                go.await();
                trigger.process(second.id());
                return null;
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, diag001.invocationCount());
        assertEquals(1, store.findByDomainId(domain.id()).size());
    }

    @Test
    void storeAloneDoesNotTrigger() {
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 1L)), "rid-store", 1609459200L).getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        store.store(parse.parse(receipt.id()));

        assertEquals(0, diag001.invocationCount());
        assertEquals(1, store.findByDomainId(domain.id()).size());
    }

    @Test
    void doesNotTriggerDiag002Diag003OrHealth001() {
        DmarcReportReceipt receipt = receive.receive(domain.id(),
            xml(List.of(record("192.0.2.1", 1L)), "rid-scope", 1609459200L).getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        trigger.process(receipt.id());

        assertEquals(1, diag001.invocationCount());
        Set<String> ctorParams = Arrays.stream(DmarcIngestTriggerService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of(
            "DmarcReportParseService",
            "DmarcAggregateEvidenceService",
            "DmarcAggregateEvidenceRepository",
            "Diag001AlignmentDerivationPort"), ctorParams);
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.diagnosis.application.RootCauseService"));
        assertTrue(Arrays.stream(DmarcIngestTriggerService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Health")
                || f.getType().getSimpleName().contains("ReObservation")));
    }

    @Test
    void processIsNotAnHttpEndpointOrScheduler() {
        assertTrue(Arrays.stream(DmarcIngestTriggerService.class.getDeclaredAnnotations())
            .noneMatch(a -> a.annotationType().getSimpleName().contains("Controller")
                || a.annotationType().getSimpleName().contains("RequestMapping")
                || a.annotationType().getSimpleName().contains("Scheduled")));
        assertTrue(Arrays.stream(DmarcIngestTriggerService.class.getDeclaredMethods())
            .filter(m -> "process".equals(m.getName()))
            .map(Method::getReturnType)
            .noneMatch(t -> t.getSimpleName().contains("ResponseEntity")));
        assertTrue(Arrays.stream(DmarcIngestTriggerService.class.getDeclaredMethods())
            .noneMatch(m -> Arrays.stream(m.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType().getSimpleName().contains("Scheduled")
                    || a.annotationType().getSimpleName().contains("Mapping"))));
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

    private static String conflictingRecord(String sourceIp, long count) {
        return """
            <record>
              <row>
                <source_ip>%s</source_ip>
                <count>%d</count>
                <policy_evaluated>
                  <disposition>quarantine</disposition>
                  <dkim>fail</dkim>
                  <spf>pass</spf>
                </policy_evaluated>
              </row>
              <identifiers>
                <header_from>other.example</header_from>
              </identifiers>
            </record>
            """.formatted(sourceIp, count);
    }
}
