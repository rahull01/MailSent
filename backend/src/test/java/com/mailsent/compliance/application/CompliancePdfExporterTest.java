package com.mailsent.compliance.application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.compliance.domain.MailboxProvider;
import com.mailsent.compliance.domain.ProviderRequirement;
import com.mailsent.compliance.domain.ProviderRequirementChecklist;
import com.mailsent.compliance.domain.ReadinessBadge;
import com.mailsent.compliance.domain.RequirementStatus;
import com.mailsent.reporting.domain.WhiteLabelConfig;

class CompliancePdfExporterTest {

    private static final String ORG_ID = "org-123";
    private static final String DOMAIN_ID = "dom-456";
    private static final String DOMAIN_NAME = "example.com";
    private static final Instant NOW = Instant.parse("2026-08-24T12:00:00Z");

    private List<ProviderRequirementChecklist> checklists;
    private ReadinessBadge badge;

    @BeforeEach
    void setUp() {
        ProviderRequirement googleSpf = ProviderRequirement.of(
            "SPF Record", "Must have valid SPF record", "Authorizes sending IP", RequirementStatus.SATISFIED, "oe-101");
        ProviderRequirement googleDkim = ProviderRequirement.of(
            "DKIM Alignment", "Must align DKIM signatures", "Authenticates message body", RequirementStatus.SATISFIED, "oe-102");
        ProviderRequirement googleDmarc = ProviderRequirement.of(
            "DMARC Policy", "Must have p=none or stronger", "Protects domain reputation", RequirementStatus.WARNING, "oe-103");

        ProviderRequirementChecklist googleChecklist = ProviderRequirementChecklist.create(
            ORG_ID, DOMAIN_ID, MailboxProvider.GOOGLE, List.of(googleSpf, googleDkim, googleDmarc), NOW);

        checklists = List.of(googleChecklist);
        badge = ReadinessBadge.fromChecklists(ORG_ID, DOMAIN_ID, checklists, NOW);
    }

    @Test
    void exportsStructurallyValidPdfBytesAndVerifiesOffsetsByteForByte() {
        byte[] pdf = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, checklists, badge, null);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF should contain sufficient vector operations");

        String pdfText = new String(pdf, StandardCharsets.US_ASCII);

        // 1. Magic Header & Binary Comment
        assertTrue(pdfText.startsWith("%PDF-1.4\n%"), "Must start with PDF 1.4 magic header");
        assertEquals((byte) 0xE2, pdf[10]);
        assertEquals((byte) 0xE3, pdf[11]);
        assertEquals((byte) 0xCF, pdf[12]);
        assertEquals((byte) 0xD3, pdf[13]);

        // 2. Objects existence
        assertTrue(pdfText.contains("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj"), "Must contain Catalog dictionary");
        assertTrue(pdfText.contains("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj"), "Must contain Pages dictionary");
        assertTrue(pdfText.contains("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792]"), "Must contain Page dictionary");
        assertTrue(pdfText.contains("/BaseFont /Helvetica >>\nendobj"), "Must contain Helvetica Font");
        assertTrue(pdfText.contains("/BaseFont /Helvetica-Bold >>\nendobj"), "Must contain Helvetica-Bold Font");

        // 3. Content Stream & Length
        assertTrue(pdfText.contains("4 0 obj\n<< /Length "), "Must contain Content stream");
        assertTrue(pdfText.contains("\nendstream\nendobj\n"), "Must terminate Content stream properly");

        // 4. Trailer & EOF
        assertTrue(pdfText.contains("trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n"), "Must contain standard trailer dictionary");
        assertTrue(pdfText.endsWith("%%EOF\n"), "Must terminate with %%EOF");

        // 5. Cross-Reference Table & Offset validation
        int startXrefIndex = pdfText.lastIndexOf("startxref\n") + 10;
        int eofIndex = pdfText.lastIndexOf("%%EOF");
        String xrefOffsetStr = pdfText.substring(startXrefIndex, eofIndex).trim();
        long xrefOffset = Long.parseLong(xrefOffsetStr);

        assertTrue(xrefOffset > 0 && xrefOffset < pdf.length);
        assertEquals('x', (char) pdf[(int) xrefOffset], "Byte at startxref offset must be 'x' of xref");
        assertEquals('r', (char) pdf[(int) xrefOffset + 1]);
        assertEquals('e', (char) pdf[(int) xrefOffset + 2]);
        assertEquals('f', (char) pdf[(int) xrefOffset + 3]);

        // 6. Detailed xref entry parsing & object header verification
        String[] lines = pdfText.substring((int) xrefOffset).split("\n");
        assertEquals("xref", lines[0].trim());
        assertEquals("0 7", lines[1].trim());
        assertEquals("0000000000 65535 f", lines[2].trim());

        for (int objIdx = 1; objIdx <= 6; objIdx++) {
            String entry = lines[2 + objIdx].trim();
            assertTrue(entry.endsWith("00000 n"));
            long offset = Long.parseLong(entry.split(" ")[0]);
            String objPrefix = objIdx + " 0 obj";
            String bytesAtOffset = new String(pdf, (int) offset, objPrefix.length(), StandardCharsets.US_ASCII);
            assertEquals(objPrefix, bytesAtOffset, "Object " + objIdx + " must start at declared xref offset");
        }

        // 7. Content stream byte-length verification
        int streamHeaderIdx = pdfText.indexOf("4 0 obj\n<< /Length ");
        int streamStartIdx = pdfText.indexOf("stream\n", streamHeaderIdx) + 7;
        int streamEndIdx = pdfText.indexOf("\nendstream", streamStartIdx);
        int actualStreamBytes = streamEndIdx - streamStartIdx;

        int lengthStart = streamHeaderIdx + "4 0 obj\n<< /Length ".length();
        int lengthEnd = pdfText.indexOf(" >>\nstream", lengthStart);
        int declaredLength = Integer.parseInt(pdfText.substring(lengthStart, lengthEnd));
        assertEquals(declaredLength, actualStreamBytes, "Declared stream /Length must equal exact byte count");
    }

    @Test
    void preservesFactualComplianceEvidenceWithoutAlteration() {
        byte[] pdf = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, checklists, badge, null);
        String pdfText = new String(pdf, StandardCharsets.US_ASCII);

        // Factual domain, provider, and evidence snapshot IDs
        assertTrue(pdfText.contains("Domain: example.com"));
        assertTrue(pdfText.contains("GOOGLE Requirements"));
        assertTrue(pdfText.contains("SPF Record"));
        assertTrue(pdfText.contains("DKIM Alignment"));
        assertTrue(pdfText.contains("DMARC Policy"));
        assertTrue(pdfText.contains("Evidence: oe-101"));
        assertTrue(pdfText.contains("Evidence: oe-102"));
        assertTrue(pdfText.contains("Evidence: oe-103"));
        assertTrue(pdfText.contains("Overall Readiness Status: NEEDS_ATTENTION"));
    }

    @Test
    void appliesWhiteLabelConfigWhenBelongingToOwningAgency() {
        WhiteLabelConfig branding = WhiteLabelConfig.of(
            ORG_ID,
            "Apex Deliverability Group",
            "https://apex.com/logo.svg",
            "#1E40AF",
            "Confidential Assessment for Board of Directors",
            "support@apex.com"
        );

        byte[] pdf = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, checklists, badge, branding);
        String pdfText = new String(pdf, StandardCharsets.US_ASCII);

        assertTrue(pdfText.contains("Apex Deliverability Group - Compliance Assessment"));
        assertTrue(pdfText.contains("Confidential Assessment for Board of Directors"));
        assertTrue(pdfText.contains("support@apex.com"));
    }

    @Test
    void rejectsCrossTenantBrandingAttachment() {
        WhiteLabelConfig crossTenantBranding = WhiteLabelConfig.of(
            "competitor-agency-org",
            "Competitor Agency",
            "https://competitor.com/logo.png",
            "#FF0000",
            "Invalid",
            "Invalid"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, checklists, badge, crossTenantBranding)
        );
        assertTrue(ex.getMessage().contains("INV-09"), "Must reject cross-tenant white-label branding");
    }

    @Test
    void handlesEscapedParenthesesBackslashesAndSpecialCharactersSafely() {
        ProviderRequirement trickyReq = ProviderRequirement.of(
            "DKIM (Key \\ 2048-bit)",
            "Description with (parentheses) and \\backslash\\",
            "Business (meaning) with special\r\nnewlines",
            RequirementStatus.UNKNOWN,
            "oe-special-(99)"
        );

        ProviderRequirementChecklist trickyChecklist = ProviderRequirementChecklist.create(
            ORG_ID, DOMAIN_ID, MailboxProvider.YAHOO, List.of(trickyReq), NOW);

        WhiteLabelConfig trickyBranding = WhiteLabelConfig.of(
            ORG_ID,
            "Agency (Global) \\ US",
            "https://agency.com/logo.svg",
            "#1E40AF",
            "Disclaimer (Confidential \\ Legal)",
            "Footer (Support)"
        );

        ReadinessBadge trickyBadge = ReadinessBadge.fromChecklists(ORG_ID, DOMAIN_ID, List.of(trickyChecklist), NOW);
        byte[] pdf = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, List.of(trickyChecklist), trickyBadge, trickyBranding);

        String pdfText = new String(pdf, StandardCharsets.US_ASCII);
        assertTrue(pdfText.contains("Agency \\(Global\\) \\\\ US"));
        assertTrue(pdfText.contains("DKIM \\(Key \\\\ 2048-bit\\)"));
        assertTrue(pdfText.contains("Evidence: oe-special-\\(99\\)"));
        assertTrue(pdfText.contains("Disclaimer \\(Confidential \\\\ Legal\\)"));
    }

    @Test
    void supportsAllRequirementStatusesAcrossMultipleProviders() {
        ProviderRequirement r1 = ProviderRequirement.of("R1", "D1", "M1", RequirementStatus.SATISFIED, "oe-1");
        ProviderRequirement r2 = ProviderRequirement.of("R2", "D2", "M2", RequirementStatus.WARNING, "oe-2");
        ProviderRequirement r3 = ProviderRequirement.of("R3", "D3", "M3", RequirementStatus.NOT_SATISFIED, "oe-3");
        ProviderRequirement r4 = ProviderRequirement.of("R4", "D4", "M4", RequirementStatus.UNKNOWN, "oe-4");

        ProviderRequirementChecklist clGoogle = ProviderRequirementChecklist.create(
            ORG_ID, DOMAIN_ID, MailboxProvider.GOOGLE, List.of(r1, r2), NOW);
        ProviderRequirementChecklist clYahoo = ProviderRequirementChecklist.create(
            ORG_ID, DOMAIN_ID, MailboxProvider.YAHOO, List.of(r3), NOW);
        ProviderRequirementChecklist clMicrosoft = ProviderRequirementChecklist.create(
            ORG_ID, DOMAIN_ID, MailboxProvider.MICROSOFT, List.of(r4), NOW);

        List<ProviderRequirementChecklist> multiCl = List.of(clGoogle, clYahoo, clMicrosoft);
        ReadinessBadge multiBadge = ReadinessBadge.fromChecklists(ORG_ID, DOMAIN_ID, multiCl, NOW);

        byte[] pdf = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, multiCl, multiBadge, null);
        String pdfText = new String(pdf, StandardCharsets.US_ASCII);

        assertTrue(pdfText.contains("GOOGLE Requirements"));
        assertTrue(pdfText.contains("YAHOO Requirements"));
        assertTrue(pdfText.contains("MICROSOFT Requirements"));
        assertTrue(pdfText.contains("[SATISFIED]"));
        assertTrue(pdfText.contains("[WARNING]"));
        assertTrue(pdfText.contains("[NOT_SATISFIED]"));
        assertTrue(pdfText.contains("[UNKNOWN]"));
    }

    @Test
    void generatesMultiPagePdfDeterministicallyWhenContentOverflows() {
        List<ProviderRequirement> manyReqs = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            manyReqs.add(ProviderRequirement.of(
                "Requirement " + i,
                "Detailed description for item " + i,
                "Business impact explanation for item " + i,
                (i % 2 == 0) ? RequirementStatus.SATISFIED : RequirementStatus.WARNING,
                "oe-" + i
            ));
        }

        ProviderRequirementChecklist largeChecklist = ProviderRequirementChecklist.create(
            ORG_ID, DOMAIN_ID, MailboxProvider.GOOGLE, manyReqs, NOW);

        ReadinessBadge largeBadge = ReadinessBadge.fromChecklists(ORG_ID, DOMAIN_ID, List.of(largeChecklist), NOW);
        byte[] pdf = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, List.of(largeChecklist), largeBadge, null);

        String pdfText = new String(pdf, StandardCharsets.US_ASCII);

        // Verify page count >= 2 in Pages dictionary
        assertTrue(pdfText.contains("2 0 obj\n<< /Type /Pages /Kids [3 0 R 5 0 R"), "Pages must contain at least 2 page object references");
        assertTrue(pdfText.contains("Page 1"), "Must include Page 1 label");
        assertTrue(pdfText.contains("Page 2"), "Must include Page 2 label");

        // Verify first and last requirements are present
        assertTrue(pdfText.contains("Requirement 1"));
        assertTrue(pdfText.contains("Evidence: oe-1"));
        assertTrue(pdfText.contains("Requirement 30"));
        assertTrue(pdfText.contains("Evidence: oe-30"));

        // Verify xref and structural validity
        int startXrefIndex = pdfText.lastIndexOf("startxref\n") + 10;
        int eofIndex = pdfText.lastIndexOf("%%EOF");
        long xrefOffset = Long.parseLong(pdfText.substring(startXrefIndex, eofIndex).trim());
        assertEquals('x', (char) pdf[(int) xrefOffset]);
    }

    @Test
    void identicalInputsProduceDeterministicIdenticalBytes() {
        byte[] pdf1 = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, checklists, badge, null);
        byte[] pdf2 = CompliancePdfExporter.export(ORG_ID, DOMAIN_ID, DOMAIN_NAME, checklists, badge, null);

        assertArrayEquals(pdf1, pdf2, "PDF generation must be byte-for-byte deterministic");
    }
}
