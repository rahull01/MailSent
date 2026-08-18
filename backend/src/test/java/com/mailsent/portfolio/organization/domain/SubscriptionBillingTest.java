package com.mailsent.portfolio.organization.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class SubscriptionBillingTest {

    @Test
    void verifiesPlanDefinitionsAndAnnualDiscount() {
        PlanDefinition starter = PlanDefinition.forTier(PlanTier.STARTER);
        assertEquals(5, starter.maxDomains());
        assertEquals(4900, starter.monthlyPriceCents());
        assertEquals(3900, starter.annualMonthlyPriceCents()); // 20% annual discount (BILL-004)

        PlanDefinition agency = PlanDefinition.forTier(PlanTier.AGENCY_PRO);
        assertEquals(50, agency.maxDomains());
        assertEquals(19900, agency.monthlyPriceCents());
        assertEquals(15900, agency.annualMonthlyPriceCents());
    }

    @Test
    void managesSubscriptionLifecycle() {
        Instant trialEnd = Instant.now().plusSeconds(86400 * 14);
        Subscription sub = Subscription.startTrial("org-1", trialEnd);

        assertNotNull(sub.id());
        assertEquals("org-1", sub.organizationId());
        assertEquals(PlanTier.AGENCY_PRO, sub.tier());
        assertEquals(Subscription.Status.TRIAL, sub.status());
        assertTrue(sub.isActiveOrTrial());

        Instant nextPeriod = Instant.now().plusSeconds(86400 * 365);
        sub.upgrade(PlanTier.ENTERPRISE, true, nextPeriod);
        assertEquals(PlanTier.ENTERPRISE, sub.tier());
        assertEquals(Subscription.Status.ACTIVE, sub.status());
        assertTrue(sub.isAnnual());

        sub.cancel();
        assertEquals(Subscription.Status.CANCELED, sub.status());
        assertFalse(sub.isActiveOrTrial());
    }

    @Test
    void enforcesFreeTierGating() {
        // Free tier (null or canceled sub)
        assertTrue(PlanTierGate.canAddDomain(null, 0));
        assertFalse(PlanTierGate.canAddDomain(null, 1)); // limit is 1

        assertTrue(PlanTierGate.isFeatureAllowed(null, "BASIC_SCAN"));
        assertFalse(PlanTierGate.isFeatureAllowed(null, "WHITE_LABEL"));

        // Active Starter sub
        Subscription starterSub = Subscription.create("org-1", PlanTier.STARTER, false, Instant.now().plusSeconds(86400));
        assertTrue(PlanTierGate.canAddDomain(starterSub, 4));
        assertFalse(PlanTierGate.canAddDomain(starterSub, 5)); // limit is 5
        assertTrue(PlanTierGate.isFeatureAllowed(starterSub, "ALERTS"));
        assertFalse(PlanTierGate.isFeatureAllowed(starterSub, "WHITE_LABEL"));
    }

    @Test
    void validatesInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                Subscription.startTrial("", Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                Subscription.create("", PlanTier.STARTER, false, Instant.now())
        );
    }
}
