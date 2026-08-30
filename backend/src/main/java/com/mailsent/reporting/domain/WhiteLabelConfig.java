package com.mailsent.reporting.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * WhiteLabelConfig — REP-004 White-label Reports (Agency Tier) (FUN-R1/R2).
 *
 * <p>Represents immutable presentation metadata for agency-tier reporting.
 * Strictly separates presentation branding (agency name, logo, accent color,
 * custom disclaimer/footer) from underlying factual and diagnostic report data.
 * Does not alter evidence, health signals, timestamps, or multi-tenant scope.</p>
 */
public final class WhiteLabelConfig {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");
    private static final String DEFAULT_ACCENT_COLOR = "#0F172A"; // Slate 900

    private final String agencyOrganizationId;
    private final String agencyName;
    private final String logoUrl;
    private final String accentColorHex;
    private final String customDisclaimer;
    private final String customFooter;

    private WhiteLabelConfig(String agencyOrganizationId, String agencyName, String logoUrl,
                             String accentColorHex, String customDisclaimer, String customFooter) {
        this.agencyOrganizationId = agencyOrganizationId;
        this.agencyName = agencyName;
        this.logoUrl = logoUrl;
        this.accentColorHex = accentColorHex;
        this.customDisclaimer = customDisclaimer;
        this.customFooter = customFooter;
    }

    /**
     * Creates a customized white-label configuration bound to an Agency Organization (INV-09).
     *
     * @param agencyOrganizationId the owning Agency Organization ID (optional/null for global default)
     * @param agencyName           the agency display name (required, non-blank)
     * @param logoUrl              the logo URL/path (optional; must be https:// or /relative/path)
     * @param accentColorHex       the 6-character hex color (e.g. #1E40AF) or null for default
     * @param customDisclaimer     optional custom legal disclaimer text
     * @param customFooter         optional custom footer text
     * @return an immutable WhiteLabelConfig instance
     */
    public static WhiteLabelConfig of(String agencyOrganizationId, String agencyName, String logoUrl,
                                      String accentColorHex, String customDisclaimer, String customFooter) {
        if (agencyName == null || agencyName.isBlank()) {
            throw new IllegalArgumentException("An agency name is required for white-label configuration.");
        }
        String cleanOrgId = (agencyOrganizationId != null && !agencyOrganizationId.isBlank())
            ? agencyOrganizationId.trim() : null;
        String cleanAgencyName = agencyName.trim();

        String cleanLogoUrl = null;
        if (logoUrl != null && !logoUrl.isBlank()) {
            cleanLogoUrl = validateLogoUrl(logoUrl.trim());
        }

        String cleanColor = DEFAULT_ACCENT_COLOR;
        if (accentColorHex != null && !accentColorHex.isBlank()) {
            cleanColor = validateHexColor(accentColorHex.trim());
        }

        String cleanDisclaimer = (customDisclaimer != null && !customDisclaimer.isBlank())
            ? customDisclaimer.trim() : null;

        String cleanFooter = (customFooter != null && !customFooter.isBlank())
            ? customFooter.trim() : null;

        return new WhiteLabelConfig(cleanOrgId, cleanAgencyName, cleanLogoUrl, cleanColor, cleanDisclaimer, cleanFooter);
    }

    /**
     * Creates a customized white-label configuration without explicit org binding.
     */
    public static WhiteLabelConfig of(String agencyName, String logoUrl, String accentColorHex,
                                      String customDisclaimer, String customFooter) {
        return of(null, agencyName, logoUrl, accentColorHex, customDisclaimer, customFooter);
    }

    /**
     * Creates a basic white-label configuration with agency name only.
     */
    public static WhiteLabelConfig of(String agencyName) {
        return of(null, agencyName, null, null, null, null);
    }

    /**
     * Creates default neutral MailSent branding.
     */
    public static WhiteLabelConfig defaultBranding() {
        return new WhiteLabelConfig(null, "MailSent", null, DEFAULT_ACCENT_COLOR, null, null);
    }

    private static String validateHexColor(String hex) {
        if (!HEX_COLOR_PATTERN.matcher(hex).matches()) {
            throw new IllegalArgumentException("Invalid accent color format. Must be a 6-digit hex color (e.g. #1E40AF).");
        }
        return hex.toUpperCase();
    }

    private static String validateLogoUrl(String url) {
        String lower = url.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")
            || lower.contains("<") || lower.contains(">") || lower.contains("\"")) {
            throw new IllegalArgumentException("Unsafe logo URL protocol or payload rejected.");
        }
        if (!url.startsWith("https://") && !url.startsWith("/")) {
            throw new IllegalArgumentException("Logo URL must begin with https:// or be an absolute relative path starting with /.");
        }
        return url;
    }

    public String agencyOrganizationId() {
        return agencyOrganizationId;
    }

    public String agencyName() {
        return agencyName;
    }

    public String logoUrl() {
        return logoUrl;
    }

    public String accentColorHex() {
        return accentColorHex;
    }

    public String customDisclaimer() {
        return customDisclaimer;
    }

    public String customFooter() {
        return customFooter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhiteLabelConfig that)) return false;
        return Objects.equals(agencyOrganizationId, that.agencyOrganizationId) &&
               Objects.equals(agencyName, that.agencyName) &&
               Objects.equals(logoUrl, that.logoUrl) &&
               Objects.equals(accentColorHex, that.accentColorHex) &&
               Objects.equals(customDisclaimer, that.customDisclaimer) &&
               Objects.equals(customFooter, that.customFooter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agencyOrganizationId, agencyName, logoUrl, accentColorHex, customDisclaimer, customFooter);
    }
}
