package com.mailsent.portfolio.organization.domain;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * PlanDefinition — Configured boundaries, limits, and pricing for a plan tier (BILL-001 / BILL-004).
 */
public class PlanDefinition {

    private final PlanTier tier;
    private final int maxDomains;
    private final int cadenceMinutes;
    private final int monthlyPriceCents;
    private final int annualMonthlyPriceCents; // with discount
    private final Set<String> allowedFeatures;

    public PlanDefinition(PlanTier tier, int maxDomains, int cadenceMinutes,
                          int monthlyPriceCents, int annualMonthlyPriceCents,
                          Set<String> allowedFeatures) {
        this.tier = Objects.requireNonNull(tier);
        this.maxDomains = maxDomains;
        this.cadenceMinutes = cadenceMinutes;
        this.monthlyPriceCents = monthlyPriceCents;
        this.annualMonthlyPriceCents = annualMonthlyPriceCents;
        this.allowedFeatures = Collections.unmodifiableSet(Objects.requireNonNull(allowedFeatures));
    }

    public static PlanDefinition forTier(PlanTier tier) {
        switch (tier) {
            case FREE:
                return new PlanDefinition(
                        PlanTier.FREE,
                        1,
                        1440, // 24h
                        0,
                        0,
                        Set.of("BASIC_SCAN", "EXPLANATION")
                );
            case STARTER:
                return new PlanDefinition(
                        PlanTier.STARTER,
                        5,
                        60, // 1h
                        4900, // $49/mo
                        3900, // $39/mo annual (20% discount)
                        Set.of("BASIC_SCAN", "EXPLANATION", "SCHEDULED_REPORTS", "ALERTS")
                );
            case AGENCY_PRO:
                return new PlanDefinition(
                        PlanTier.AGENCY_PRO,
                        50,
                        15, // 15m
                        19900, // $199/mo
                        15900, // $159/mo annual
                        Set.of("BASIC_SCAN", "EXPLANATION", "SCHEDULED_REPORTS", "ALERTS", "MULTI_CLIENT", "AI_INSIGHTS", "CONSENT_LANE")
                );
            case ENTERPRISE:
                return new PlanDefinition(
                        PlanTier.ENTERPRISE,
                        1000,
                        5, // 5m
                        99900, // $999/mo
                        79900, // $799/mo annual
                        Set.of("BASIC_SCAN", "EXPLANATION", "SCHEDULED_REPORTS", "ALERTS", "MULTI_CLIENT", "AI_INSIGHTS", "CONSENT_LANE", "WHITE_LABEL", "AUDIT_EXPORT")
                );
            default:
                throw new IllegalArgumentException("Unknown plan tier: " + tier);
        }
    }

    public PlanTier tier() { return tier; }
    public int maxDomains() { return maxDomains; }
    public int cadenceMinutes() { return cadenceMinutes; }
    public int monthlyPriceCents() { return monthlyPriceCents; }
    public int annualMonthlyPriceCents() { return annualMonthlyPriceCents; }
    public Set<String> allowedFeatures() { return allowedFeatures; }
}
