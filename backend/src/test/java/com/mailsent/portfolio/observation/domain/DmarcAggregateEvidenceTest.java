package com.mailsent.portfolio.observation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

class DmarcAggregateEvidenceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");

    @Test
    void fromParsedTakesIdentityFromParsedReportNotPolicyDomain() {
        Domain domain = Domain.register("org-1", "example.com", NOW);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive(domain, "<xml/>".getBytes(), NOW);
        ParsedDmarcReport parsed = ParsedDmarcReport.fromReceipt(
            receipt, "rid-1", "google.com", Instant.ofEpochSecond(1609459200L),
            Instant.ofEpochSecond(1609545599L), "other.example", "quarantine", "r", "s",
            List.of());

        DmarcAggregateEvidence snapshot = DmarcAggregateEvidence.fromParsed(
            parsed, "192.0.2.1", LocalDate.of(2021, 1, 1), 3L, "fail", "pass", "fail", "pass",
            "bounce.mail.example", "example.com", List.of("sel.example.com"), "none");

        assertEquals("org-1", snapshot.organizationId());
        assertEquals(domain.id(), snapshot.domainId());
        assertEquals("example.com", snapshot.domainName());
        assertEquals(receipt.id(), snapshot.receiptId());
        assertEquals("rid-1", snapshot.reportId());
        assertEquals("192.0.2.1", snapshot.sourceIp());
        assertEquals(LocalDate.of(2021, 1, 1), snapshot.utcDay());
        assertEquals(3L, snapshot.count());
        assertEquals("other.example", parsed.policyDomain());
        assertEquals("example.com", snapshot.domainName());
    }

    @Test
    void fromParsedRequiresParsedReportAndReportId() {
        assertThrows(IllegalArgumentException.class,
            () -> DmarcAggregateEvidence.fromParsed(
                null, "192.0.2.1", LocalDate.of(2021, 1, 1), 1L, null, null, null, null,
                null, null, List.of(), null));
    }

    @Test
    void restoreAdoptsStoredFacts() {
        DmarcAggregateEvidence snapshot = DmarcAggregateEvidence.restore(
            "agg-1", "org-1", "dom-1", "example.com", "rec-1", "rid-1", "192.0.2.1",
            LocalDate.of(2021, 1, 1), 3L, "fail", "pass", "fail", "pass",
            "bounce.mail.example", "example.com", List.of("sel.example.com"), "none");

        assertEquals("agg-1", snapshot.id());
        assertEquals(List.of("sel.example.com"), snapshot.dkimDomains());
        assertEquals("none", snapshot.disposition());
    }
}
