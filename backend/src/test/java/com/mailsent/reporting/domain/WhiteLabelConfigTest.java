package com.mailsent.reporting.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WhiteLabelConfigTest {

    @Test
    void constructsValidWhiteLabelConfig() {
        WhiteLabelConfig config = WhiteLabelConfig.of(
            "Apex Deliverability Agency",
            "https://cdn.example.com/logos/apex.png",
            "#1E40AF",
            "Confidential Agency Report",
            "support@apexagency.com"
        );

        assertNotNull(config);
        assertEquals("Apex Deliverability Agency", config.agencyName());
        assertEquals("https://cdn.example.com/logos/apex.png", config.logoUrl());
        assertEquals("#1E40AF", config.accentColorHex());
        assertEquals("Confidential Agency Report", config.customDisclaimer());
        assertEquals("support@apexagency.com", config.customFooter());
    }

    @Test
    void acceptsRelativeLogoPath() {
        WhiteLabelConfig config = WhiteLabelConfig.of(
            "Apex Agency",
            "/assets/branding/logo.svg",
            "#0F172A",
            null,
            null
        );

        assertEquals("/assets/branding/logo.svg", config.logoUrl());
    }

    @Test
    void rejectsBlankOrNullAgencyName() {
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of(null, "https://example.com/logo.png", "#1E40AF", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("   ", "https://example.com/logo.png", "#1E40AF", null, null));
    }

    @Test
    void rejectsInvalidHexAccentColors() {
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", null, "red", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", null, "#12345", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", null, "#1234567", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", null, "1E40AF", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", null, "#ZZZZZZ", null, null));
    }

    @Test
    void rejectsUnsafeLogoProtocolsAndPayloads() {
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", "javascript:alert(1)", "#1E40AF", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", "data:text/html,<script>alert(1)</script>", "#1E40AF", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", "vbscript:msgbox", "#1E40AF", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", "http://insecure.com/logo.png", "#1E40AF", null, null));
        assertThrows(IllegalArgumentException.class, () ->
            WhiteLabelConfig.of("Agency", "https://example.com/<script>", "#1E40AF", null, null));
    }

    @Test
    void defaultBrandingProvidesNeutralFallbacks() {
        WhiteLabelConfig def = WhiteLabelConfig.defaultBranding();

        assertEquals("MailSent", def.agencyName());
        assertNull(def.logoUrl());
        assertEquals("#0F172A", def.accentColorHex());
        assertNull(def.customDisclaimer());
        assertNull(def.customFooter());
    }

    @Test
    void normalizesWhitespaceOnAllFields() {
        WhiteLabelConfig config = WhiteLabelConfig.of(
            "  Apex Agency  ",
            "  https://example.com/logo.png  ",
            "  #1e40af  ",
            "  Disclaimer text  ",
            "  Footer text  "
        );

        assertEquals("Apex Agency", config.agencyName());
        assertEquals("https://example.com/logo.png", config.logoUrl());
        assertEquals("#1E40AF", config.accentColorHex());
        assertEquals("Disclaimer text", config.customDisclaimer());
        assertEquals("Footer text", config.customFooter());
    }
}
