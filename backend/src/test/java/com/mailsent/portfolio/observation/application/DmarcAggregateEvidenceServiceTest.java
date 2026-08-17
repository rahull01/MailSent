package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.Rfc7489DmarcAggregateXmlParser;

/**
 * Unit tests for {@link DmarcAggregateEvidenceService} (INT-005) — grouping,
 * Receipt identity, idempotency, and no OE/Alignment/Health/DIAG side effects.
 */
class DmarcAggregateEvidenceServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");

    private InMemoryDomainRepository domains;
    private InMemoryDmarcReportReceiptRepository receipts;
    private InMemoryObservationEvidenceRepository observationEvidence;
    private InMemoryDmarcAggregateEvidenceRepository aggregate;
    private DmarcReportReceiveService receive;
    private DmarcReportParseService parse;
    private DmarcAggregateEvidenceService store;
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
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void successfulParsedReportPersistsAggregateEvidence() {
        ParsedDmarcReport parsed = parseUsable(xml(List.of(record("192.0.2.1", 3L)), "1.2.3.4.999", 1609459200L));

        List<DmarcAggregateEvidence> stored = store.store(parsed);

        assertEquals(1, stored.size());
        DmarcAggregateEvidence snapshot = stored.get(0);
        assertEquals("192.0.2.1", snapshot.sourceIp());
        assertEquals(3L, snapshot.count());
        assertEquals("fail", snapshot.spfResult());
        assertEquals("pass", snapshot.dkimResult());
        assertEquals("fail", snapshot.spfAligned());
        assertEquals("pass", snapshot.dkimAligned());
        assertEquals("bounce.mail.example", snapshot.envelopeFrom());
        assertEquals("example.com", snapshot.headerFrom());
        assertEquals(List.of("sel.example.com"), snapshot.dkimDomains());
        assertEquals("none", snapshot.disposition());
        assertEquals(snapshot.id(), store.findById(snapshot.id()).orElseThrow().id());
    }

    @Test
    void lineageIsReceiptOrganizationAndDomainNotXmlPolicyDomain() {
        ParsedDmarcReport parsed = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-policy", 1609459200L));

        DmarcAggregateEvidence snapshot = store.store(parsed).get(0);

        assertEquals("org-1", snapshot.organizationId());
        assertEquals(domain.id(), snapshot.domainId());
        assertEquals("example.com", snapshot.domainName());
        assertEquals(parsed.receiptId(), snapshot.receiptId());
        assertNotEquals("other.example", snapshot.domainName());
        assertEquals("other.example", parsed.policyDomain());
    }

    @Test
    void groupsBySourceIpAndSumsHomogeneousCounts() {
        String body = xml(List.of(
            record("192.0.2.1", 2L),
            record("192.0.2.1", 4L)), "rid-group", 1609459200L);
        ParsedDmarcReport parsed = parseUsable(body);

        List<DmarcAggregateEvidence> stored = store.store(parsed);

        assertEquals(1, stored.size());
        assertEquals("192.0.2.1", stored.get(0).sourceIp());
        assertEquals(6L, stored.get(0).count());
        assertEquals(LocalDate.of(2021, 1, 1), stored.get(0).utcDay());
    }

    @Test
    void utcDayComesFromDateRangeBegin() {
        ParsedDmarcReport parsed = parseUsable(xml(List.of(record("192.0.2.8", 1L)), "rid-day", 1735689600L));

        DmarcAggregateEvidence snapshot = store.store(parsed).get(0);

        assertEquals(LocalDate.of(2025, 1, 1), snapshot.utcDay());
        assertEquals(Instant.ofEpochSecond(1735689600L), parsed.dateRangeBegin());
    }

    @Test
    void differentSourceIpsAreSeparateRows() {
        String body = xml(List.of(
            record("192.0.2.1", 1L),
            record("192.0.2.2", 9L)), "rid-ips", 1609459200L);
        ParsedDmarcReport parsed = parseUsable(body);

        List<DmarcAggregateEvidence> stored = store.store(parsed);

        assertEquals(2, stored.size());
        assertEquals(Set.of("192.0.2.1", "192.0.2.2"),
            stored.stream().map(DmarcAggregateEvidence::sourceIp).collect(Collectors.toSet()));
        assertEquals(1L, stored.stream().filter(s -> s.sourceIp().equals("192.0.2.1")).findFirst().orElseThrow().count());
        assertEquals(9L, stored.stream().filter(s -> s.sourceIp().equals("192.0.2.2")).findFirst().orElseThrow().count());
        assertTrue(stored.stream().allMatch(s -> s.reportId().equals("rid-ips")));
        assertTrue(stored.stream().allMatch(s -> s.receiptId().equals(parsed.receiptId())));
    }

    @Test
    void differentUtcDaysAreSeparateAcrossReports() {
        ParsedDmarcReport first = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-day-a", 1609459200L));
        ParsedDmarcReport second = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-day-b", 1735689600L));

        DmarcAggregateEvidence dayA = store.store(first).get(0);
        DmarcAggregateEvidence dayB = store.store(second).get(0);

        assertEquals(LocalDate.of(2021, 1, 1), dayA.utcDay());
        assertEquals(LocalDate.of(2025, 1, 1), dayB.utcDay());
        assertEquals(2, store.findByDomainId(domain.id()).size());
        assertNotEquals(dayA.reportId(), dayB.reportId());
    }

    @Test
    void reportIdLineageIsPreserved() {
        ParsedDmarcReport parsed = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "google:abc-123", 1609459200L));

        DmarcAggregateEvidence snapshot = store.store(parsed).get(0);

        assertEquals("google:abc-123", snapshot.reportId());
        assertEquals("google:abc-123", parsed.reportId());
        assertEquals(parsed.receiptId(), snapshot.receiptId());
    }

    @Test
    void duplicateReportIdAndDomainIsIdempotent() {
        byte[] payload = xml(List.of(record("192.0.2.1", 3L), record("192.0.2.2", 1L)), "same-rid", 1609459200L)
            .getBytes(StandardCharsets.UTF_8);
        ParsedDmarcReport firstParsed = parseUsable(payload);
        List<DmarcAggregateEvidence> first = store.store(firstParsed);

        ParsedDmarcReport secondParsed = parseUsable(payload);
        List<DmarcAggregateEvidence> second = store.store(secondParsed);

        assertEquals(2, first.size());
        assertEquals(first.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()),
            second.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()));
        assertEquals(2, store.findByDomainId(domain.id()).size());
        assertNotEquals(firstParsed.receiptId(), secondParsed.receiptId());
        assertEquals(first.get(0).receiptId(), second.get(0).receiptId());
    }

    @Test
    void observationEvidenceIsUntouched() {
        store.store(parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-oe", 1609459200L)));

        assertTrue(observationEvidence.findByDomainId(domain.id()).isEmpty());
        assertTrue(observationEvidence.findByOrganizationId("org-1").isEmpty());
        assertTrue(Arrays.stream(DmarcAggregateEvidenceService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("ObservationEvidence")));
    }

    @Test
    void doesNotCreateAlignmentHealthOrDiagnosis() {
        store.store(parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-diag", 1609459200L)));

        assertTrue(Arrays.stream(DmarcAggregateEvidenceService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Alignment")
                || f.getType().getSimpleName().contains("Health")
                || f.getType().getSimpleName().contains("Diagnosis")));
        assertTrue(Arrays.stream(DmarcAggregateEvidenceService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Trigger")
                || f.getType().getSimpleName().contains("Diag001")));
        Set<String> ctorParams = Arrays.stream(DmarcAggregateEvidenceService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("DmarcAggregateEvidenceRepository"), ctorParams);
        assertTrue(Arrays.stream(DmarcAggregateEvidenceService.class.getDeclaredMethods())
            .map(Method::getName)
            .noneMatch(n -> n.toLowerCase().contains("diagnos") || n.toLowerCase().contains("align")
                || n.toLowerCase().contains("health") || n.toLowerCase().contains("trigger")));
    }

    @Test
    void storeIsNotAnHttpEndpointAndPortHasNoUpdateOrDelete() {
        assertTrue(Arrays.stream(DmarcAggregateEvidenceService.class.getDeclaredAnnotations())
            .noneMatch(a -> a.annotationType().getSimpleName().contains("Controller")
                || a.annotationType().getSimpleName().contains("RequestMapping")));
        Set<String> names = Arrays.stream(DmarcAggregateEvidenceRepository.class.getMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.contains("save"));
        assertTrue(names.contains("findById"));
        assertTrue(names.contains("findByDomainId"));
        assertTrue(names.contains("findByOrganizationId"));
        assertTrue(names.contains("findByDomainIdAndReportId"));
        assertTrue(names.contains("persistNewSnapshotSet"));
        assertTrue(names.stream().noneMatch(n -> n.startsWith("update") || n.startsWith("delete")
            || n.equals("remove")));
    }

    @Test
    void unusableParsePersistsNothing() {
        DmarcReportReceipt receipt = receive.receive(domain.id(), "<not-feedback/>".getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        DmarcReportParseOutcome outcome = parse.parse(receipt.id());

        assertFalse(outcome.usable());
        assertTrue(store.store(outcome).isEmpty());
        assertTrue(store.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void organizationListingIsIsolated() {
        Domain other = Domain.register("org-2", "other.com", NOW);
        domains.save(other);
        ParsedDmarcReport forOne = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-org-1", 1609459200L));
        DmarcReportReceipt otherReceipt = receive.receive(other.id(),
            xml(List.of(record("192.0.2.9", 1L)), "rid-org-2", 1609459200L).getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        ParsedDmarcReport forTwo = parse.parse(otherReceipt.id()).parsed().orElseThrow();

        store.store(forOne);
        store.store(forTwo);

        assertEquals(1, store.findByOrganizationId("org-1").size());
        assertEquals(1, store.findByOrganizationId("org-2").size());
        assertEquals(domain.id(), store.findByOrganizationId("org-1").get(0).domainId());
    }

    @Test
    void laterGroupConflictPersistsZeroRowsAndAllowsRetry() {
        String reportId = "rid-later-conflict";
        String conflicting = xml(List.of(
            record("192.0.2.1", 1L),
            record("192.0.2.2", 1L),
            conflictingRecord("192.0.2.2", 2L)), reportId, 1609459200L);
        ParsedDmarcReport parsed = parseUsable(conflicting);

        assertThrows(IllegalStateException.class, () -> store.store(parsed));
        assertTrue(store.findByDomainId(domain.id()).isEmpty());

        ParsedDmarcReport corrected = parseUsable(xml(List.of(
            record("192.0.2.1", 1L),
            record("192.0.2.2", 2L)), reportId, 1609459200L));
        List<DmarcAggregateEvidence> stored = store.store(corrected);

        assertEquals(2, stored.size());
        assertEquals(Set.of("192.0.2.1", "192.0.2.2"),
            stored.stream().map(DmarcAggregateEvidence::sourceIp).collect(Collectors.toSet()));
    }

    @Test
    void concurrentStoresForSameDomainAndReportIdPersistOneSnapshotSet() throws Exception {
        byte[] payload = xml(List.of(record("192.0.2.1", 1L), record("192.0.2.2", 1L)),
            "rid-concurrent", 1609459200L).getBytes(StandardCharsets.UTF_8);
        ParsedDmarcReport first = parseUsable(payload);
        ParsedDmarcReport second = parseUsable(payload);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Future<List<DmarcAggregateEvidence>> a = pool.submit(() -> {
                ready.countDown();
                go.await();
                return store.store(first);
            });
            Future<List<DmarcAggregateEvidence>> b = pool.submit(() -> {
                ready.countDown();
                go.await();
                return store.store(second);
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            List<DmarcAggregateEvidence> left = a.get(5, TimeUnit.SECONDS);
            List<DmarcAggregateEvidence> right = b.get(5, TimeUnit.SECONDS);
            assertEquals(2, left.size());
            assertEquals(2, right.size());
            assertEquals(left.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()),
                right.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()));
            assertEquals(2, store.findByDomainId(domain.id()).size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void sameReportIdOnDifferentDomainsStoresIndependently() {
        Domain other = Domain.register("org-2", "other.com", NOW);
        domains.save(other);
        ParsedDmarcReport forOne = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "shared-rid", 1609459200L));
        DmarcReportReceipt otherReceipt = receive.receive(other.id(),
            xml(List.of(record("192.0.2.9", 1L)), "shared-rid", 1609459200L).getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        ParsedDmarcReport forTwo = parse.parse(otherReceipt.id()).parsed().orElseThrow();

        store.store(forOne);
        store.store(forTwo);

        assertEquals(1, store.findByDomainId(domain.id()).size());
        assertEquals(1, store.findByDomainId(other.id()).size());
        assertEquals("shared-rid", store.findByDomainId(domain.id()).get(0).reportId());
        assertEquals("shared-rid", store.findByDomainId(other.id()).get(0).reportId());
    }

    @Test
    void concurrentLosingStoreDoesNotCountAsCreatedNew() throws Exception {
        byte[] payload = xml(List.of(record("192.0.2.1", 1L)), "rid-created", 1609459200L)
            .getBytes(StandardCharsets.UTF_8);
        ParsedDmarcReport first = parseUsable(payload);
        ParsedDmarcReport second = parseUsable(payload);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Future<DmarcAggregateStoreResult> a = pool.submit(() -> {
                ready.countDown();
                go.await();
                return store.storeResult(first);
            });
            Future<DmarcAggregateStoreResult> b = pool.submit(() -> {
                ready.countDown();
                go.await();
                return store.storeResult(second);
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            List<DmarcAggregateStoreResult> results = new ArrayList<>();
            results.add(a.get(5, TimeUnit.SECONDS));
            results.add(b.get(5, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(DmarcAggregateStoreResult::createdNew).count());
            assertEquals(1, results.stream().filter(r -> !r.createdNew()).count());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void occupancyHitWaitsUntilEvidenceIsVisibleThenCreatedNewIsFalse() {
        ParsedDmarcReport parsed = parseUsable(xml(List.of(record("192.0.2.1", 1L)), "rid-wait", 1609459200L));
        List<DmarcAggregateEvidence> winnerRows = new DmarcAggregateEvidenceService(aggregate).store(parsed);
        DelayedVisibilityRepository delayed = new DelayedVisibilityRepository(winnerRows, 2);
        DmarcAggregateEvidenceService waiting = new DmarcAggregateEvidenceService(delayed);

        DmarcAggregateStoreResult result = waiting.storeResult(parsed);

        assertFalse(result.createdNew());
        assertEquals(winnerRows.get(0).id(), result.snapshots().get(0).id());
        assertEquals(2, delayed.findsBeforeVisible());
    }

    private static final class DelayedVisibilityRepository implements DmarcAggregateEvidenceRepository {
        private final List<DmarcAggregateEvidence> winner;
        private final int emptyFinds;
        private final AtomicInteger finds = new AtomicInteger();

        private DelayedVisibilityRepository(List<DmarcAggregateEvidence> winner, int emptyFinds) {
            this.winner = winner;
            this.emptyFinds = emptyFinds;
        }

        int findsBeforeVisible() {
            return emptyFinds;
        }

        @Override
        public DmarcAggregateEvidence save(DmarcAggregateEvidence evidence) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DmarcAggregateStoreResult persistNewSnapshotSet(List<DmarcAggregateEvidence> snapshots) {
            throw new DuplicateDmarcAggregateOccupancyException(
                snapshots.get(0).domainId(), snapshots.get(0).reportId(), null);
        }

        @Override
        public Optional<DmarcAggregateEvidence> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<DmarcAggregateEvidence> findByDomainId(String domainId) {
            return List.of();
        }

        @Override
        public List<DmarcAggregateEvidence> findByOrganizationId(String organizationId) {
            return List.of();
        }

        @Override
        public List<DmarcAggregateEvidence> findByDomainIdAndReportId(String domainId, String reportId) {
            if (finds.getAndIncrement() < emptyFinds) {
                return List.of();
            }
            return winner;
        }
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

    private ParsedDmarcReport parseUsable(String xml) {
        return parseUsable(xml.getBytes(StandardCharsets.UTF_8));
    }

    private ParsedDmarcReport parseUsable(byte[] payload) {
        DmarcReportReceipt receipt = receive.receive(domain.id(), payload).orElseThrow();
        return parse.parse(receipt.id()).parsed().orElseThrow();
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
                <adkim>r</adkim>
                <aspf>s</aspf>
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
