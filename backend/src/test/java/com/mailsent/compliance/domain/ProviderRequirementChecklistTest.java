package com.mailsent.compliance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProviderRequirementChecklistTest {

    @Test
    void createsChecklistWithReadyReadiness() {
        ProviderRequirement req1 = ProviderRequirement.of(
                "SPF Authentication",
                "Valid SPF record published",
                "Mailbox providers can verify sending IP legitimacy",
                RequirementStatus.SATISFIED,
                "ev-spf-123"
        );
        ProviderRequirement req2 = ProviderRequirement.of(
                "DKIM Signature",
                "Valid DKIM signature aligned",
                "Prevents message tampering in transit",
                RequirementStatus.SATISFIED,
                "ev-dkim-456"
        );

        Instant now = Instant.now();
        ProviderRequirementChecklist checklist = ProviderRequirementChecklist.create(
                "org-1",
                "domain-1",
                MailboxProvider.GOOGLE,
                List.of(req1, req2),
                now
        );

        assertNotNull(checklist.id());
        assertEquals("org-1", checklist.organizationId());
        assertEquals("domain-1", checklist.domainId());
        assertEquals(MailboxProvider.GOOGLE, checklist.provider());
        assertEquals(ReadinessLevel.READY, checklist.overallReadiness());
        assertEquals(2, checklist.requirements().size());
        assertEquals(now, checklist.assessedAt());
    }

    @Test
    void createsChecklistWithNotReadyWhenUnsatisfiedExists() {
        ProviderRequirement req1 = ProviderRequirement.of(
                "SPF Authentication",
                "Valid SPF record published",
                "Mailbox providers can verify sending IP legitimacy",
                RequirementStatus.SATISFIED,
                "ev-spf-123"
        );
        ProviderRequirement req2 = ProviderRequirement.of(
                "DMARC Enforcement",
                "DMARC policy at p=reject or p=quarantine",
                "Protects domain from impersonation",
                RequirementStatus.NOT_SATISFIED,
                "ev-dmarc-789"
        );

        ProviderRequirementChecklist checklist = ProviderRequirementChecklist.create(
                "org-1",
                "domain-1",
                MailboxProvider.YAHOO,
                List.of(req1, req2),
                Instant.now()
        );

        assertEquals(ReadinessLevel.NOT_READY, checklist.overallReadiness());
    }

    @Test
    void createsChecklistWithAtRiskWhenWarningExists() {
        ProviderRequirement req1 = ProviderRequirement.of(
                "SPF Authentication",
                "Valid SPF record published",
                "Mailbox providers can verify sending IP legitimacy",
                RequirementStatus.SATISFIED,
                "ev-spf-123"
        );
        ProviderRequirement req2 = ProviderRequirement.of(
                "Spam Rate Baseline",
                "Spam rate trending near 0.25%",
                "Google requires spam rate below 0.30%",
                RequirementStatus.WARNING,
                "ev-spam-101"
        );

        ProviderRequirementChecklist checklist = ProviderRequirementChecklist.create(
                "org-1",
                "domain-1",
                MailboxProvider.GOOGLE,
                List.of(req1, req2),
                Instant.now()
        );

        assertEquals(ReadinessLevel.AT_RISK, checklist.overallReadiness());
    }

    @Test
    void validatesArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                ProviderRequirement.of("   ", "desc", "meaning", RequirementStatus.SATISFIED, null)
        );

        assertThrows(IllegalArgumentException.class, () ->
                ProviderRequirementChecklist.create("", "domain-1", MailboxProvider.GOOGLE, List.of(), Instant.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
                ProviderRequirementChecklist.create("org-1", "", MailboxProvider.GOOGLE, List.of(), Instant.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
                ProviderRequirementChecklist.create("org-1", "domain-1", MailboxProvider.GOOGLE, List.of(), Instant.now())
        );
    }
}
