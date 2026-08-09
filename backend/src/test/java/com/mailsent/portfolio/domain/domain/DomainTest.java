package com.mailsent.portfolio.domain.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Domain} aggregate — the ORG-003 invariants (required owner,
 * required/unique case-folded name, verbatim restore).
 */
class DomainTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");
    private static final String ORG = "org-1";

    @Test
    void registerFoldsCaseAndTrimsName() {
        Domain domain = Domain.register(ORG, "  Example.COM  ", NOW);
        assertEquals("example.com", domain.name());
        assertEquals(ORG, domain.organizationId());
    }

    @Test
    void registerRequiresOwningOrganization() {
        assertThrows(IllegalArgumentException.class, () -> Domain.register(null, "example.com", NOW));
        assertThrows(IllegalArgumentException.class, () -> Domain.register("  ", "example.com", NOW));
    }

    @Test
    void registerRequiresName() {
        assertThrows(IllegalArgumentException.class, () -> Domain.register(ORG, null, NOW));
        assertThrows(IllegalArgumentException.class, () -> Domain.register(ORG, "   ", NOW));
    }

    @Test
    void registerRequiresCreatedAt() {
        assertThrows(IllegalArgumentException.class, () -> Domain.register(ORG, "example.com", null));
    }

    @Test
    void normalizeFoldsToCanonicalForm() {
        assertEquals("mail.example.com", Domain.normalize("  MAIL.Example.COM  "));
    }

    @Test
    void restoreAdoptsStoredIdentityWithoutRenormalizing() {
        Domain domain = Domain.restore("dom-1", ORG, "Example.COM", NOW);
        assertEquals("dom-1", domain.id());
        assertEquals(ORG, domain.organizationId());
        // restore adopts stored values verbatim (like Organization.restore), so the
        // stored name is preserved as-is rather than re-folded.
        assertEquals("Example.COM", domain.name());
    }
}