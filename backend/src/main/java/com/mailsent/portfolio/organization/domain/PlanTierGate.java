package com.mailsent.portfolio.organization.domain;

/**
 * PlanTierGate — Enforces domain capacity and feature access gating (BILL-003).
 */
public final class PlanTierGate {

    private PlanTierGate() {}

    public static boolean canAddDomain(Subscription subscription, int currentDomainCount) {
        if (subscription == null || !subscription.isActiveOrTrial()) {
            PlanDefinition freePlan = PlanDefinition.forTier(PlanTier.FREE);
            return currentDomainCount < freePlan.maxDomains();
        }
        PlanDefinition plan = PlanDefinition.forTier(subscription.tier());
        return currentDomainCount < plan.maxDomains();
    }

    public static boolean isFeatureAllowed(Subscription subscription, String featureName) {
        if (subscription == null || !subscription.isActiveOrTrial()) {
            return PlanDefinition.forTier(PlanTier.FREE).allowedFeatures().contains(featureName);
        }
        return PlanDefinition.forTier(subscription.tier()).allowedFeatures().contains(featureName);
    }
}
