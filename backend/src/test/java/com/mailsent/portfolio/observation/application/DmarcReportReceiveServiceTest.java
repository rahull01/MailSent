package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;

/**
 * Unit tests for {@link DmarcReportReceiveService} (INT-003) — registered Domain
 * uuid, discard unknown, opaque bytes, org from Domain, OE untouched, no
 * parsed/aggregate evidence, no DIAG/health/alignment.
 */
class DmarcReportReceiveServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");
    private static final byte[] PAYLOAD = "opaque-rua-bytes".getBytes();

    private InMemoryDomainRepository domains;
    private InMemoryDmarcReportReceiptRepository receipts;
    private InMemoryObservationEvidenceRepository evidence;
    private DmarcReportReceiveService service;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        receipts = new InMemoryDmarcReportReceiptRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        service = new DmarcReportReceiveService(receipts, domains);
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void unknownUuidIsDiscardedAndNothingIsPersisted() {
        Optional<DmarcReportReceipt> result = service.receive(UUID.randomUUID().toString(), PAYLOAD);

        assertTrue(result.isEmpty());
        assertTrue(receipts.findByOrganizationId("org-1").isEmpty());
        assertTrue(evidence.findByOrganizationId("org-1").isEmpty());
    }

    @Test
    void blankUuidIsDiscarded() {
        assertTrue(service.receive("  ", PAYLOAD).isEmpty());
        assertTrue(service.receive(null, PAYLOAD).isEmpty());
        assertTrue(receipts.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void registeredDomainUuidPersistsOpaqueBytesAndOrgFromDomain() {
        DmarcReportReceipt saved = service.receive(domain.id(), PAYLOAD).orElseThrow();

        assertEquals(domain.organizationId(), saved.organizationId());
        assertEquals(domain.id(), saved.domainId());
        assertEquals(domain.name(), saved.domainName());
        assertArrayEquals(PAYLOAD, saved.payload());
        assertNull(saved.reportId());
        assertTrue(receipts.findById(saved.id()).isPresent());
        assertEquals(1, service.findByDomainId(domain.id()).size());
        assertEquals(1, service.findByOrganizationId("org-1").size());
    }

    @Test
    void receiveDoesNotAcceptCallerSuppliedOrganization() {
        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getMethods())
            .noneMatch(m -> "receive".equals(m.getName()) && m.getParameterCount() > 2));

        DmarcReportReceipt saved = service.receive(domain.id(), PAYLOAD).orElseThrow();
        assertEquals("org-1", saved.organizationId());
    }

    @Test
    void observationEvidenceIsUntouched() {
        service.receive(domain.id(), PAYLOAD);

        assertTrue(evidence.findByDomainId(domain.id()).isEmpty());
        assertTrue(evidence.findByOrganizationId("org-1").isEmpty());
        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("ObservationEvidence")));
    }

    @Test
    void receiveDoesNotParseXmlOrCreateAggregateEvidence() {
        byte[] xmlWithReportId = ("<feedback><report_metadata><report_id>must-not-be-read</report_id>"
            + "</report_metadata></feedback>").getBytes();
        DmarcReportReceipt saved = service.receive(domain.id(), xmlWithReportId).orElseThrow();

        assertNull(saved.reportId());
        assertArrayEquals(xmlWithReportId, saved.payload());
        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Parse")
                || f.getType().getSimpleName().contains("DmarcAggregate")));
        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .noneMatch(t -> t.getSimpleName().contains("DmarcAggregate")));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.mailsent.portfolio.observation.application.DmarcAggregateService"));
    }

    @Test
    void doesNotCreateAlignmentHealthSignalOrTriggerDiagnosis() {
        service.receive(domain.id(), PAYLOAD);

        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Alignment")
                || f.getType().getSimpleName().contains("Health")
                || f.getType().getSimpleName().contains("Diagnosis")));
        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Trigger")
                || f.getType().getSimpleName().contains("Diag001")));
        Set<String> ctorParams = Arrays.stream(DmarcReportReceiveService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("DmarcReportReceiptRepository", "DomainRepository"), ctorParams);
        assertTrue(Arrays.stream(DmarcReportReceiveService.class.getDeclaredMethods())
            .map(Method::getName)
            .noneMatch(n -> n.toLowerCase().contains("diagnos") || n.toLowerCase().contains("align")
                || n.toLowerCase().contains("parse") || n.toLowerCase().contains("xml")));
    }

    @Test
    void duplicateBytesBeforeParseMayYieldTwoReceipts() {
        DmarcReportReceipt first = service.receive(domain.id(), PAYLOAD).orElseThrow();
        DmarcReportReceipt second = service.receive(domain.id(), PAYLOAD).orElseThrow();

        assertNotEquals(first.id(), second.id());
        assertEquals(2, service.findByDomainId(domain.id()).size());
        assertNull(first.reportId());
        assertNull(second.reportId());
    }

    @Test
    void portHasNoUpdateOrDelete() {
        Set<String> names = Arrays.stream(DmarcReportReceiptRepository.class.getMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.contains("save"));
        assertTrue(names.contains("findById"));
        assertTrue(names.contains("associateReportId"));
        assertTrue(names.stream().noneMatch(n -> n.startsWith("update") || n.startsWith("delete")
            || n.equals("remove")));
    }

    @Test
    void findByIdAndFindByDomainIdRoundTrip() {
        DmarcReportReceipt saved = service.receive(domain.id(), PAYLOAD).orElseThrow();
        assertEquals(saved.id(), service.findById(saved.id()).orElseThrow().id());
        List<DmarcReportReceipt> listed = service.findByDomainId(domain.id());
        assertEquals(1, listed.size());
        assertArrayEquals(PAYLOAD, listed.get(0).payload());
    }
}
