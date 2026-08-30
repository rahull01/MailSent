package com.mailsent.reporting.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReportTest {

    @Test
    void generatesValidDomainReport() {
        String domainId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String content = "Health Score: 100\nStatus: Healthy";

        Report report = Report.generate(domainId, Report.TargetScope.DOMAIN, content, now);

        assertNotNull(report.id());
        assertEquals(domainId, report.targetId());
        assertEquals(Report.TargetScope.DOMAIN, report.scope());
        assertEquals(content, report.contentBody());
        assertEquals(now, report.generatedAt());
        assertEquals(WhiteLabelConfig.defaultBranding(), report.whiteLabelConfig());
    }

    @Test
    void generatesWhiteLabelPortfolioReportWithoutAlteringFactualContent() {
        String portfolioId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String factualContent = "Aggregate Delivery Confidence: 94%\nHealthy Domains: 18\nAt-Risk Domains: 2";
        WhiteLabelConfig branding = WhiteLabelConfig.of(
            "Apex Deliverability",
            "https://apex.com/logo.svg",
            "#1E40AF",
            "Confidential Agency Report",
            "Apex Agency 2026"
        );

        Report report = Report.generate(portfolioId, Report.TargetScope.PORTFOLIO, factualContent, now, branding);

        assertNotNull(report.id());
        assertEquals(portfolioId, report.targetId());
        assertEquals(Report.TargetScope.PORTFOLIO, report.scope());
        assertEquals(factualContent, report.contentBody(), "Factual report body must remain strictly uncorrupted");
        assertEquals(now, report.generatedAt());
        assertEquals(branding, report.whiteLabelConfig());
        assertEquals("Apex Deliverability", report.whiteLabelConfig().agencyName());
    }

    @Test
    void allowsBrandingWhenAgencyTargetMatchesAgencyBranding() {
        String agencyAId = "agency-org-a";
        Instant now = Instant.now();
        String content = "Portfolio Health: 98%";
        WhiteLabelConfig agencyABranding = WhiteLabelConfig.of(
            agencyAId,
            "Agency Alpha",
            "https://alpha.com/logo.svg",
            "#1E40AF",
            "Confidential",
            "Alpha Support"
        );

        Report report = Report.generate(agencyAId, Report.TargetScope.PORTFOLIO, content, now, agencyABranding);

        assertNotNull(report);
        assertEquals(agencyAId, report.targetId());
        assertEquals(agencyABranding, report.whiteLabelConfig());
    }

    @Test
    void rejectsCrossTenantBrandingAttachmentBetweenDifferentAgencies() {
        String agencyAId = "agency-org-a";
        String agencyBId = "agency-org-b";
        Instant now = Instant.now();
        String content = "Portfolio Health: 98%";

        WhiteLabelConfig agencyABranding = WhiteLabelConfig.of(
            agencyAId,
            "Agency Alpha",
            "https://alpha.com/logo.svg",
            "#1E40AF",
            "Confidential",
            "Alpha Support"
        );

        // Agency A branding + Agency B target = rejected (INV-09)
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Report.generate(agencyBId, Report.TargetScope.PORTFOLIO, content, now, agencyABranding)
        );
        assertEquals("WhiteLabelConfig owned by agency 'agency-org-a' cannot be attached to a report targeting organization 'agency-org-b' (INV-09).", ex.getMessage());
    }

    @Test
    void rejectsEmptyContentBody() {
        assertThrows(IllegalArgumentException.class, () -> 
            Report.generate(UUID.randomUUID().toString(), Report.TargetScope.PORTFOLIO, "   ", Instant.now())
        );
    }
}
