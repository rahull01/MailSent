package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * Unit tests for {@link ParsedDmarcReport} — identity from Receipt, required
 * report_id, policy domain is a fact not tenant identity.
 */
class ParsedDmarcReportTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");

    @Test
    void fromReceiptTakesIdentityFromReceiptNotPolicyDomain() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive(domain, "<feedback/>".getBytes(), NOW);

        ParsedDmarcReport parsed = ParsedDmarcReport.fromReceipt(
            receipt,
            "rid-1",
            "google.com",
            Instant.ofEpochSecond(1609459200L),
            Instant.ofEpochSecond(1609545599L),
            "other.example",
            "none",
            "r",
            "r",
            List.of());

        assertEquals(receipt.id(), parsed.receiptId());
        assertEquals("org-1", parsed.organizationId());
        assertEquals(domain.id(), parsed.domainId());
        assertEquals("example.com", parsed.domainName());
        assertEquals("rid-1", parsed.reportId());
        assertEquals("other.example", parsed.policyDomain());
    }

    @Test
    void fromReceiptRejectsMissingReportId() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive(domain, new byte[0], NOW);
        assertThrows(IllegalArgumentException.class,
            () -> ParsedDmarcReport.fromReceipt(receipt, "  ", null, null, null, null, null, null, null, List.of()));
        assertThrows(IllegalArgumentException.class,
            () -> ParsedDmarcReport.fromReceipt(null, "rid", null, null, null, null, null, null, null, List.of()));
    }
}
