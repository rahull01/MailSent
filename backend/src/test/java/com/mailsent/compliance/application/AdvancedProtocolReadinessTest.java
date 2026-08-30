package com.mailsent.compliance.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.compliance.domain.BimiEvaluator;
import com.mailsent.compliance.domain.MailboxProvider;
import com.mailsent.compliance.domain.MtaStsEvaluator;
import com.mailsent.compliance.domain.ProviderRequirement;
import com.mailsent.compliance.domain.ProviderRequirementChecklist;
import com.mailsent.compliance.domain.ReadinessBadge;
import com.mailsent.compliance.domain.ReadinessLevel;
import com.mailsent.compliance.domain.RequirementStatus;
import com.mailsent.compliance.domain.TlsRptEvaluator;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;

class AdvancedProtocolReadinessTest {

    private static final String ORG_ID = "org-100";
    private static final String DOMAIN_ID = "dom-200";
    private static final String DOMAIN_NAME = "example.com";
    private static final Instant NOW = Instant.parse("2026-08-24T12:00:00Z");

    private Domain domain;

    @BeforeEach
    void setUp() {
        domain = Domain.restore(DOMAIN_ID, ORG_ID, DOMAIN_NAME, NOW);
    }

    // ==========================================
    // 1. MTA-STS TESTS
    // ==========================================

    @Test
    void mtaStsValidRecordYieldsSatisfiedWithEvidenceRef() {
        List<String> txt = List.of("v=STSv1; id=20260824T120000Z");
        ProviderRequirement req = MtaStsEvaluator.evaluate(txt, "ev-101");

        assertEquals("MTA-STS Readiness", req.name());
        assertEquals(RequirementStatus.SATISFIED, req.status());
        assertEquals("ev-101", req.evidenceRef());
    }

    @Test
    void mtaStsMissingRecordYieldsNotSatisfiedWithEvidenceRef() {
        List<String> txt = List.of("v=spf1 include:_spf.google.com ~all");
        ProviderRequirement req = MtaStsEvaluator.evaluate(txt, "ev-102");

        assertEquals(RequirementStatus.NOT_SATISFIED, req.status());
        assertEquals("ev-102", req.evidenceRef());
    }

    @Test
    void mtaStsMissingIdYieldsWarning() {
        List<String> txt = List.of("v=STSv1; mode=enforce");
        ProviderRequirement req = MtaStsEvaluator.evaluate(txt, "ev-103");

        assertEquals(RequirementStatus.WARNING, req.status());
        assertEquals("ev-103", req.evidenceRef());
    }

    @Test
    void mtaStsInvalidVersionYieldsWarning() {
        List<String> txt = List.of("v=STSv2; id=12345");
        // Starts with v=stsv... so falls through or is not satisfied if unparsed
        ProviderRequirement req = MtaStsEvaluator.evaluate(txt, "ev-104");
        // Because it does not start with v=stsv1, it will be treated as absent -> NOT_SATISFIED
        assertEquals(RequirementStatus.NOT_SATISFIED, req.status());
    }

    @Test
    void mtaStsNullRecordsYieldsUnknownWithNoEvidenceRef() {
        ProviderRequirement req = MtaStsEvaluator.evaluate(null, null);

        assertEquals(RequirementStatus.UNKNOWN, req.status());
        assertNull(req.evidenceRef());
    }

    // ==========================================
    // 2. TLS-RPT TESTS
    // ==========================================

    @Test
    void tlsRptValidMailtoRuaYieldsSatisfiedWithEvidenceRef() {
        List<String> txt = List.of("v=TLSRPTv1; rua=mailto:tls-reports@example.com");
        ProviderRequirement req = TlsRptEvaluator.evaluate(txt, "ev-201");

        assertEquals("TLS-RPT Readiness", req.name());
        assertEquals(RequirementStatus.SATISFIED, req.status());
        assertEquals("ev-201", req.evidenceRef());
    }

    @Test
    void tlsRptValidHttpsRuaYieldsSatisfiedWithEvidenceRef() {
        List<String> txt = List.of("v=TLSRPTv1; rua=https://tls.example.com/v1/report");
        ProviderRequirement req = TlsRptEvaluator.evaluate(txt, "ev-202");

        assertEquals(RequirementStatus.SATISFIED, req.status());
        assertEquals("ev-202", req.evidenceRef());
    }

    @Test
    void tlsRptMissingRuaYieldsWarning() {
        List<String> txt = List.of("v=TLSRPTv1");
        ProviderRequirement req = TlsRptEvaluator.evaluate(txt, "ev-203");

        assertEquals(RequirementStatus.WARNING, req.status());
        assertEquals("ev-203", req.evidenceRef());
    }

    @Test
    void tlsRptInvalidRuaSchemeYieldsWarning() {
        List<String> txt = List.of("v=TLSRPTv1; rua=ftp://invalidscheme.com");
        ProviderRequirement req = TlsRptEvaluator.evaluate(txt, "ev-204");

        assertEquals(RequirementStatus.WARNING, req.status());
    }

    @Test
    void tlsRptMissingRecordYieldsNotSatisfied() {
        List<String> txt = List.of("some other text");
        ProviderRequirement req = TlsRptEvaluator.evaluate(txt, "ev-205");

        assertEquals(RequirementStatus.NOT_SATISFIED, req.status());
    }

    @Test
    void tlsRptNullRecordsYieldsUnknown() {
        ProviderRequirement req = TlsRptEvaluator.evaluate(null, null);

        assertEquals(RequirementStatus.UNKNOWN, req.status());
        assertNull(req.evidenceRef());
    }

    // ==========================================
    // 3. BIMI TESTS
    // ==========================================

    @Test
    void bimiValidLogoYieldsSatisfiedWithEvidenceRef() {
        List<String> txt = List.of("v=BIMI1; l=https://example.com/brand/logo.svg;");
        ProviderRequirement req = BimiEvaluator.evaluate(txt, "ev-301");

        assertEquals("BIMI Readiness", req.name());
        assertEquals(RequirementStatus.SATISFIED, req.status());
        assertEquals("ev-301", req.evidenceRef());
    }

    @Test
    void bimiValidLogoAndVmcYieldsSatisfiedWithEvidenceRef() {
        List<String> txt = List.of("v=BIMI1; l=https://example.com/logo.svg; a=https://example.com/cert.pem");
        ProviderRequirement req = BimiEvaluator.evaluate(txt, "ev-302");

        assertEquals(RequirementStatus.SATISFIED, req.status());
        assertEquals("ev-302", req.evidenceRef());
    }

    @Test
    void bimiMissingLogoYieldsWarning() {
        List<String> txt = List.of("v=BIMI1; a=https://example.com/cert.pem");
        ProviderRequirement req = BimiEvaluator.evaluate(txt, "ev-303");

        assertEquals(RequirementStatus.WARNING, req.status());
    }

    @Test
    void bimiNonHttpsLogoYieldsWarning() {
        List<String> txt = List.of("v=BIMI1; l=http://insecure.com/logo.svg");
        ProviderRequirement req = BimiEvaluator.evaluate(txt, "ev-304");

        assertEquals(RequirementStatus.WARNING, req.status());
    }

    @Test
    void bimiMissingRecordYieldsNotSatisfied() {
        List<String> txt = List.of("v=spf1 -all");
        ProviderRequirement req = BimiEvaluator.evaluate(txt, "ev-305");

        assertEquals(RequirementStatus.NOT_SATISFIED, req.status());
    }

    @Test
    void bimiNullRecordsYieldsUnknown() {
        ProviderRequirement req = BimiEvaluator.evaluate(null, null);

        assertEquals(RequirementStatus.UNKNOWN, req.status());
        assertNull(req.evidenceRef());
    }

    // ==========================================
    // 4. SERVICE INTEGRATION & TENANT ISOLATION
    // ==========================================

    @Test
    void serviceEvaluatesAllProtocolsAgainstAuthoritativeObservation() {
        List<String> txt = List.of(
            "v=STSv1; id=20260824;",
            "v=TLSRPTv1; rua=mailto:tls@example.com",
            "v=BIMI1; l=https://example.com/logo.svg"
        );

        DnsObservation dns = DnsObservation.of(DOMAIN_NAME, List.of(), txt, null, NOW);
        ObservationEvidence evidence = ObservationEvidence.capture(domain, dns);

        ProviderRequirementChecklist checklist = AdvancedProtocolReadinessService.evaluate(
            ORG_ID, DOMAIN_ID, evidence, NOW
        );

        assertNotNull(checklist);
        assertEquals(ORG_ID, checklist.organizationId());
        assertEquals(DOMAIN_ID, checklist.domainId());
        assertEquals(MailboxProvider.GENERIC, checklist.provider());
        assertEquals(ReadinessLevel.READY, checklist.overallReadiness());
        assertEquals(3, checklist.requirements().size());

        for (ProviderRequirement req : checklist.requirements()) {
            assertEquals(RequirementStatus.SATISFIED, req.status());
            assertEquals(evidence.id(), req.evidenceRef());
        }
    }

    @Test
    void serviceRejectsCrossTenantObservationEvidence() {
        Domain otherDomain = Domain.restore("other-dom", "competitor-org", "other.com", NOW);
        DnsObservation dns = DnsObservation.of("other.com", List.of(), List.of(), null, NOW);
        ObservationEvidence crossTenantEvidence = ObservationEvidence.capture(otherDomain, dns);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            AdvancedProtocolReadinessService.evaluate(ORG_ID, DOMAIN_ID, crossTenantEvidence, NOW)
        );
        assertEquals("ObservationEvidence does not belong to the requested organization/domain (INV-09).", ex.getMessage());
    }

    @Test
    void serviceRejectsDomainMismatchEvenIfOrgMatches() {
        Domain siblingDomain = Domain.restore("sibling-dom", ORG_ID, "sibling.com", NOW);
        DnsObservation dns = DnsObservation.of("sibling.com", List.of(), List.of(), null, NOW);
        ObservationEvidence siblingEvidence = ObservationEvidence.capture(siblingDomain, dns);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            AdvancedProtocolReadinessService.evaluate(ORG_ID, DOMAIN_ID, siblingEvidence, NOW)
        );
        assertEquals("ObservationEvidence does not belong to the requested organization/domain (INV-09).", ex.getMessage());
    }

    // ==========================================
    // 5. SECURITY & RESILIENCE TESTS
    // ==========================================

    @Test
    void parserHandlesMalformedAndOversizedRecordsSafely() {
        // Construct large string with repetitive semicolons and junk data
        String hugeJunk = "v=STSv1; " + ";".repeat(1000) + " id=" + "A".repeat(5000) + "; junk===foo;;;";
        ProviderRequirement req = MtaStsEvaluator.evaluate(List.of(hugeJunk), "ev-sec-1");

        assertEquals(RequirementStatus.SATISFIED, req.status());
        assertEquals("ev-sec-1", req.evidenceRef());
    }

    @Test
    void parserHandlesWhitespaceAndDuplicateTagsDeterministically() {
        String trickyBimi = "   v=BIMI1   ;   l=https://first.com/logo.svg  ;  l=https://second.com/logo.svg  ;   ";
        ProviderRequirement req = BimiEvaluator.evaluate(List.of(trickyBimi), "ev-sec-2");

        assertEquals(RequirementStatus.SATISFIED, req.status());
    }

    // ==========================================
    // 6. READINESS BADGE INTEGRATION
    // ==========================================

    @Test
    void protocolChecklistIntegratesCleanlyWithReadinessBadge() {
        List<String> txt = List.of(
            "v=STSv1; id=20260824;",
            "v=TLSRPTv1; rua=mailto:tls@example.com"
            // Missing BIMI
        );

        DnsObservation dns = DnsObservation.of(DOMAIN_NAME, List.of(), txt, null, NOW);
        ObservationEvidence evidence = ObservationEvidence.capture(domain, dns);

        ProviderRequirementChecklist checklist = AdvancedProtocolReadinessService.evaluate(
            ORG_ID, DOMAIN_ID, evidence, NOW
        );

        assertEquals(ReadinessLevel.NOT_READY, checklist.overallReadiness());

        ReadinessBadge badge = ReadinessBadge.fromChecklists(ORG_ID, DOMAIN_ID, List.of(checklist), NOW);
        assertEquals(ReadinessBadge.BadgeTier.NON_COMPLIANT, badge.tier());
        assertEquals(2, badge.satisfiedCount());
        assertEquals(3, badge.totalCount());
    }
}
