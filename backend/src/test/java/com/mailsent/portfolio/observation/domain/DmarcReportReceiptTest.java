package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * Unit tests for {@link DmarcReportReceipt} (INT-003) — receive from Domain +
 * opaque bytes, restore, nullable report_id, and the absence of parse/alignment
 * /health/diagnosis fields.
 */
class DmarcReportReceiptTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00.123456789Z");
    private static final byte[] BYTES = "<feedback/>".getBytes();

    @Test
    void receiveTakesIdentityFromDomainLeavesReportIdNullAndCopiesPayload() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive(domain, BYTES, NOW);

        assertEquals("org-1", receipt.organizationId());
        assertEquals(domain.id(), receipt.domainId());
        assertEquals("example.com", receipt.domainName());
        assertNull(receipt.reportId());
        assertArrayEquals(BYTES, receipt.payload());
        assertEquals(NOW.truncatedTo(ChronoUnit.MICROS), receipt.receivedAt());
        receipt.payload()[0] = 'x';
        assertArrayEquals(BYTES, receipt.payload());
    }

    @Test
    void restoreAdoptsStoredValuesIncludingNullableReportId() {
        DmarcReportReceipt receipt = DmarcReportReceipt.restore(
            "rcpt-1", "org-1", "dom-1", "example.com", null, BYTES, NOW);

        assertEquals("rcpt-1", receipt.id());
        assertEquals("org-1", receipt.organizationId());
        assertEquals("dom-1", receipt.domainId());
        assertNull(receipt.reportId());
        assertArrayEquals(BYTES, receipt.payload());
    }

    @Test
    void restoreMayCarryReportIdFilledLaterWithoutMakingThisAParsedReport() {
        DmarcReportReceipt receipt = DmarcReportReceipt.restore(
            "rcpt-1", "org-1", "dom-1", "example.com", "reporter:1", BYTES, NOW);
        assertEquals("reporter:1", receipt.reportId());
        Set<String> names = Arrays.stream(DmarcReportReceipt.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("parse")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("aggregat")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("align")));
    }

    @Test
    void withAssociatedReportIdFillsNullLineageWithoutChangingPayload() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt received = DmarcReportReceipt.receive(domain, BYTES, NOW);
        DmarcReportReceipt associated = received.withAssociatedReportId("reporter:1");

        assertEquals(received.id(), associated.id());
        assertEquals("reporter:1", associated.reportId());
        assertArrayEquals(BYTES, associated.payload());
        assertNull(received.reportId());
        assertEquals(associated, associated.withAssociatedReportId("other"));
        assertEquals("reporter:1", associated.withAssociatedReportId("other").reportId());
    }

    @Test
    void receiveRequiresDomainPayloadAndInstant() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        assertThrows(IllegalArgumentException.class,
            () -> DmarcReportReceipt.receive(null, BYTES, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> DmarcReportReceipt.receive(domain, null, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> DmarcReportReceipt.receive(domain, BYTES, null));
    }

    @Test
    void receiveAllowsEmptyPayload() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive(domain, new byte[0], NOW);
        assertEquals(0, receipt.payload().length);
        assertNull(receipt.reportId());
    }

    @Test
    void twoReceivesAreDistinctAppendOnlySnapshots() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt first = DmarcReportReceipt.receive(domain, BYTES, NOW);
        DmarcReportReceipt second = DmarcReportReceipt.receive(domain, BYTES, NOW);
        assertNotEquals(first.id(), second.id());
    }

    @Test
    void surfaceHasNoHealthAlignmentDiagnosisOrParsedReportFields() {
        Set<String> names = Arrays.stream(DmarcReportReceipt.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("health")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("diagnos")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("xml")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("envelope")));
        assertTrue(names.stream().noneMatch(n -> n.toLowerCase().contains("headerfrom")));
    }
}
