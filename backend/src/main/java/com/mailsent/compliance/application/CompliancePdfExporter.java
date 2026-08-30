package com.mailsent.compliance.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mailsent.compliance.domain.ProviderRequirement;
import com.mailsent.compliance.domain.ProviderRequirementChecklist;
import com.mailsent.compliance.domain.ReadinessBadge;
import com.mailsent.reporting.domain.WhiteLabelConfig;

/**
 * CompliancePdfExporter — COMP-003 Compliance PDF Report Export (FUN-C3).
 *
 * <p>Pure-Java, zero-dependency PDF 1.4 vector generator. Produces structurally
 * valid single-page and multi-page PDF documents containing single-domain compliance assessments
 * (COMP-001 / COMP-002) with deterministic evidence lineage (INV-11) and multi-tenant
 * isolation (INV-09).</p>
 */
public final class CompliancePdfExporter {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneOffset.UTC);

    private CompliancePdfExporter() {}

    /**
     * Exports a domain compliance assessment to standards-compliant PDF 1.4 bytes.
     *
     * @param organizationId   the owning organization ID (required)
     * @param domainId         the domain ID (required)
     * @param domainName       the domain name (required)
     * @param checklists       the provider checklists (required, non-empty)
     * @param badge            the aggregated readiness badge (required)
     * @param whiteLabelConfig optional agency white-label branding
     * @return structurally valid PDF byte array
     * @throws IllegalArgumentException on missing inputs or tenant isolation violations (INV-09)
     */
    public static byte[] export(String organizationId, String domainId, String domainName,
                                List<ProviderRequirementChecklist> checklists,
                                ReadinessBadge badge, WhiteLabelConfig whiteLabelConfig) {
        Objects.requireNonNull(organizationId, "organizationId is required");
        Objects.requireNonNull(domainId, "domainId is required");
        Objects.requireNonNull(domainName, "domainName is required");
        Objects.requireNonNull(checklists, "checklists is required");
        Objects.requireNonNull(badge, "badge is required");

        if (organizationId.isBlank() || domainId.isBlank() || domainName.isBlank()) {
            throw new IllegalArgumentException("IDs and domainName must not be blank.");
        }
        if (checklists.isEmpty()) {
            throw new IllegalArgumentException("Checklists must not be empty.");
        }

        // Validate checklist tenant ownership
        for (ProviderRequirementChecklist cl : checklists) {
            if (!organizationId.equals(cl.organizationId()) || !domainId.equals(cl.domainId())) {
                throw new IllegalArgumentException("Checklist organizationId/domainId mismatch (INV-09).");
            }
        }

        // Validate badge tenant ownership
        if (!organizationId.equals(badge.organizationId()) || !domainId.equals(badge.domainId())) {
            throw new IllegalArgumentException("ReadinessBadge organizationId/domainId mismatch (INV-09).");
        }

        // Validate WhiteLabelConfig agency tenant boundary (INV-09)
        WhiteLabelConfig branding = whiteLabelConfig != null ? whiteLabelConfig : WhiteLabelConfig.defaultBranding();
        if (branding.agencyOrganizationId() != null && !organizationId.equals(branding.agencyOrganizationId())) {
            throw new IllegalArgumentException("WhiteLabelConfig owned by agency '" + branding.agencyOrganizationId()
                    + "' cannot be attached to a report for organization '" + organizationId + "' (INV-09).");
        }

        return generatePdf(organizationId, domainId, domainName, checklists, badge, branding);
    }

    private static byte[] generatePdf(String organizationId, String domainId, String domainName,
                                      List<ProviderRequirementChecklist> checklists,
                                      ReadinessBadge badge, WhiteLabelConfig branding) {
        List<List<String>> pagesOps = new ArrayList<>();
        List<String> currentOps = new ArrayList<>();

        // Start Page 1
        addPageChrome(currentOps, branding, domainName, organizationId, domainId, badge, 1);
        int y = 575;

        for (ProviderRequirementChecklist cl : checklists) {
            // Need at least 50pt for section header + 1 requirement
            if (y < 120) {
                pagesOps.add(currentOps);
                currentOps = new ArrayList<>();
                addPageChrome(currentOps, branding, domainName, organizationId, domainId, badge, pagesOps.size() + 1);
                y = 660;
            }

            currentOps.add("0.90 0.92 0.95 rg 50 " + (y - 18) + " 512 22 re f");
            currentOps.add("BT /F2 11 Tf 0.06 0.09 0.16 rg 60 " + (y - 12) + " Td (" + escape(cl.provider().name() + " Requirements - Readiness: " + cl.overallReadiness().name()) + ") Tj ET");
            y -= 30;

            for (ProviderRequirement req : cl.requirements()) {
                if (y < 90) {
                    pagesOps.add(currentOps);
                    currentOps = new ArrayList<>();
                    addPageChrome(currentOps, branding, domainName, organizationId, domainId, badge, pagesOps.size() + 1);
                    y = 660;
                }

                String statusColor = switch (req.status()) {
                    case SATISFIED -> "0.06 0.60 0.30 rg";
                    case WARNING -> "0.85 0.50 0.05 rg";
                    case NOT_SATISFIED -> "0.85 0.20 0.20 rg";
                    case UNKNOWN -> "0.45 0.50 0.58 rg";
                };

                currentOps.add("BT /F2 9 Tf " + statusColor + " 65 " + y + " Td ([" + escape(req.status().name()) + "]) Tj ET");
                currentOps.add("BT /F2 9 Tf 0.10 0.15 0.20 rg 165 " + y + " Td (" + escape(req.name()) + ") Tj ET");
                String evRef = req.evidenceRef() != null ? req.evidenceRef() : "NONE";
                currentOps.add("BT /F1 8 Tf 0.40 0.45 0.55 rg 380 " + y + " Td (Evidence: " + escape(evRef) + ") Tj ET");
                y -= 14;

                currentOps.add("BT /F1 8 Tf 0.30 0.35 0.45 rg 80 " + y + " Td (" + escape(req.businessMeaning()) + ") Tj ET");
                y -= 18;
            }
            y -= 10;
        }

        // Add final page
        pagesOps.add(currentOps);

        // Add footer to all pages
        for (List<String> pageOps : pagesOps) {
            if (branding.customDisclaimer() != null) {
                pageOps.add("BT /F1 8 Tf 0.45 0.50 0.58 rg 50 55 Td (" + escape(branding.customDisclaimer()) + ") Tj ET");
            }
            if (branding.customFooter() != null) {
                pageOps.add("BT /F1 8 Tf 0.45 0.50 0.58 rg 50 43 Td (" + escape(branding.customFooter()) + ") Tj ET");
            }
        }

        return assemblePdf(pagesOps);
    }

    private static void addPageChrome(List<String> ops, WhiteLabelConfig branding, String domainName,
                                      String organizationId, String domainId, ReadinessBadge badge, int pageNumber) {
        // Background card
        ops.add("0.96 0.97 0.98 rg 36 36 540 720 re f");
        ops.add("0.85 0.88 0.92 RG 1 w 36 36 540 720 re S");

        // Header accent bar
        ops.add("0.06 0.09 0.16 rg 36 716 540 40 re f");

        // Header Title
        ops.add("BT /F2 16 Tf 1 1 1 rg 50 730 Td (" + escape(branding.agencyName() + " - Compliance Assessment") + ") Tj ET");

        // Metadata box
        ops.add("BT /F2 12 Tf 0.06 0.09 0.16 rg 50 690 Td (Domain: " + escape(domainName) + ") Tj ET");
        ops.add("BT /F1 9 Tf 0.35 0.40 0.48 rg 50 675 Td (Organization ID: " + escape(organizationId) + "  |  Domain ID: " + escape(domainId) + ") Tj ET");
        ops.add("BT /F1 9 Tf 0.35 0.40 0.48 rg 50 662 Td (Assessed: " + escape(ISO_FMT.format(badge.computedAt())) + "  |  Page " + pageNumber + ") Tj ET");

        if (pageNumber == 1) {
            // Overall Readiness Summary on page 1
            ops.add("0.92 0.94 0.97 rg 50 605 512 45 re f");
            ops.add("0.80 0.84 0.90 RG 1 w 50 605 512 45 re S");
            ops.add("BT /F2 11 Tf 0.06 0.09 0.16 rg 65 632 Td (Overall Readiness Status: " + escape(badge.tier().name()) + ") Tj ET");
            ops.add("BT /F1 10 Tf 0.20 0.25 0.33 rg 65 617 Td (Satisfied: " + badge.satisfiedCount() + " of " + badge.totalCount() + " requirements satisfied across providers) Tj ET");
        }
    }

    private static byte[] assemblePdf(List<List<String>> pagesOps) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Long> offsets = new ArrayList<>();
            int numPages = pagesOps.size();

            // Header
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
            out.write(new byte[] { '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n' });

            // Object 1: Catalog
            offsets.add((long) out.size());
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Object 2: Pages
            offsets.add((long) out.size());
            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < numPages; i++) {
                int pageObjNum = 3 + i * 2; // page 1 is obj 3, page 2 is obj 5...
                kids.append(pageObjNum).append(" 0 R ");
            }
            out.write(("2 0 obj\n<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + numPages + " >>\nendobj\n")
                    .getBytes(StandardCharsets.US_ASCII));

            int font1ObjNum = 3 + numPages * 2;
            int font2ObjNum = font1ObjNum + 1;

            // Generate Page and Content Stream objects
            for (int i = 0; i < numPages; i++) {
                int pageObjNum = 3 + i * 2;
                int streamObjNum = pageObjNum + 1;

                String pageContent = String.join("\n", pagesOps.get(i));
                byte[] contentBytes = pageContent.getBytes(StandardCharsets.US_ASCII);

                // Page Object
                offsets.add((long) out.size());
                String pageObj = pageObjNum + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents "
                        + streamObjNum + " 0 R /Resources << /Font << /F1 " + font1ObjNum + " 0 R /F2 " + font2ObjNum
                        + " 0 R >> >> >>\nendobj\n";
                out.write(pageObj.getBytes(StandardCharsets.US_ASCII));

                // Content Stream Object
                offsets.add((long) out.size());
                String streamHeader = streamObjNum + " 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n";
                out.write(streamHeader.getBytes(StandardCharsets.US_ASCII));
                out.write(contentBytes);
                out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));
            }

            // Font F1 (Helvetica)
            offsets.add((long) out.size());
            out.write((font1ObjNum + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n")
                    .getBytes(StandardCharsets.US_ASCII));

            // Font F2 (Helvetica-Bold)
            offsets.add((long) out.size());
            out.write((font2ObjNum + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n")
                    .getBytes(StandardCharsets.US_ASCII));

            // Cross-Reference Table
            long xrefOffset = (long) out.size();
            out.write(("xref\n0 " + (offsets.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (Long offset : offsets) {
                String line = String.format("%010d 00000 n \n", offset);
                out.write(line.getBytes(StandardCharsets.US_ASCII));
            }

            // Trailer & EOF
            out.write(("trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n"
                    + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));

            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to assemble PDF byte buffer", e);
        }
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("\r", " ")
                   .replace("\n", " ");
    }
}
