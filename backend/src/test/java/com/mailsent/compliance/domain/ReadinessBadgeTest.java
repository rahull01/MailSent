package com.mailsent.compliance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReadinessBadgeTest {

    @Test
    void computesCompliantBadge() {
        ProviderRequirement req1 = ProviderRequirement.of("SPF", "desc", "meaning", RequirementStatus.SATISFIED, "ev-1");
        ProviderRequirement req2 = ProviderRequirement.of("DKIM", "desc", "meaning", RequirementStatus.SATISFIED, "ev-2");
        ProviderRequirementChecklist cl1 = ProviderRequirementChecklist.create("org-1", "dom-1", MailboxProvider.GOOGLE, List.of(req1), Instant.now());
        ProviderRequirementChecklist cl2 = ProviderRequirementChecklist.create("org-1", "dom-1", MailboxProvider.YAHOO, List.of(req2), Instant.now());

        Instant now = Instant.now();
        ReadinessBadge badge = ReadinessBadge.fromChecklists("org-1", "dom-1", List.of(cl1, cl2), now);

        assertNotNull(badge.id());
        assertEquals("org-1", badge.organizationId());
        assertEquals("dom-1", badge.domainId());
        assertEquals(ReadinessBadge.BadgeTier.COMPLIANT, badge.tier());
        assertEquals(2, badge.satisfiedCount());
        assertEquals(2, badge.totalCount());
        assertEquals(ReadinessLevel.READY, badge.providerStatus().get(MailboxProvider.GOOGLE));
        assertEquals(ReadinessLevel.READY, badge.providerStatus().get(MailboxProvider.YAHOO));
        assertEquals(now, badge.computedAt());
    }

    @Test
    void computesNonCompliantBadgeWhenNotReadyExists() {
        ProviderRequirement req1 = ProviderRequirement.of("SPF", "desc", "meaning", RequirementStatus.SATISFIED, "ev-1");
        ProviderRequirement req2 = ProviderRequirement.of("DMARC", "desc", "meaning", RequirementStatus.NOT_SATISFIED, "ev-2");
        ProviderRequirementChecklist cl1 = ProviderRequirementChecklist.create("org-1", "dom-1", MailboxProvider.GOOGLE, List.of(req1), Instant.now());
        ProviderRequirementChecklist cl2 = ProviderRequirementChecklist.create("org-1", "dom-1", MailboxProvider.YAHOO, List.of(req2), Instant.now());

        ReadinessBadge badge = ReadinessBadge.fromChecklists("org-1", "dom-1", List.of(cl1, cl2), Instant.now());

        assertEquals(ReadinessBadge.BadgeTier.NON_COMPLIANT, badge.tier());
        assertEquals(1, badge.satisfiedCount());
        assertEquals(2, badge.totalCount());
    }

    @Test
    void computesNeedsAttentionWhenAtRisk() {
        ProviderRequirement req1 = ProviderRequirement.of("SPF", "desc", "meaning", RequirementStatus.SATISFIED, "ev-1");
        ProviderRequirement req2 = ProviderRequirement.of("Spam", "desc", "meaning", RequirementStatus.WARNING, "ev-2");
        ProviderRequirementChecklist cl1 = ProviderRequirementChecklist.create("org-1", "dom-1", MailboxProvider.GOOGLE, List.of(req1), Instant.now());
        ProviderRequirementChecklist cl2 = ProviderRequirementChecklist.create("org-1", "dom-1", MailboxProvider.MICROSOFT, List.of(req2), Instant.now());

        ReadinessBadge badge = ReadinessBadge.fromChecklists("org-1", "dom-1", List.of(cl1, cl2), Instant.now());

        assertEquals(ReadinessBadge.BadgeTier.NEEDS_ATTENTION, badge.tier());
    }

    @Test
    void validatesInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                ReadinessBadge.fromChecklists("", "dom-1", List.of(), Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                ReadinessBadge.fromChecklists("org-1", "", List.of(), Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                ReadinessBadge.fromChecklists("org-1", "dom-1", List.of(), Instant.now())
        );
    }
}
