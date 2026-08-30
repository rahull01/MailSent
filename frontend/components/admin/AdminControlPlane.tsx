"use client";

import React from "react";

export interface CustomerMetric {
  orgId: string;
  orgName: string;
  plan: "FREE" | "STARTER" | "AGENCY_PRO" | "ENTERPRISE";
  billingCadence?: "MONTHLY" | "ANNUAL";
  subscriptionStatus?: "TRIAL" | "ACTIVE" | "PAST_DUE" | "CANCELED";
  domainsMonitored: number;
  healthScore: number;
  adoptionScore: number;
  churnRisk: "LOW" | "MEDIUM" | "HIGH";
  lastActive: string;
}

export interface PlanPricing {
  monthlyPriceCents: number;
  annualMonthlyPriceCents: number;
}

// Authoritative plan definitions from domain (BILL-001 / BILL-004)
export const AUTHORITATIVE_PLAN_PRICING: Record<
  "FREE" | "STARTER" | "AGENCY_PRO" | "ENTERPRISE",
  PlanPricing
> = {
  FREE: { monthlyPriceCents: 0, annualMonthlyPriceCents: 0 },
  STARTER: { monthlyPriceCents: 4900, annualMonthlyPriceCents: 3900 },
  AGENCY_PRO: { monthlyPriceCents: 19900, annualMonthlyPriceCents: 15900 },
  ENTERPRISE: { monthlyPriceCents: 99900, annualMonthlyPriceCents: 79900 },
};

export interface PlanRevenueDistribution {
  tier: "FREE" | "STARTER" | "AGENCY_PRO" | "ENTERPRISE";
  tierName: string;
  subscriberCount: number;
  payingCount: number;
  mrrCents: number;
  percentageOfMrr: number;
}

export interface RevenueMetrics {
  totalMrrCents: number;
  arrRunRateCents: number;
  activePayingAccounts: number;
  totalSubscribers: number;
  arpuCents: number;
  tierDistribution: PlanRevenueDistribution[];
}

export interface AdminControlPlaneProps {
  totalCustomers: number;
  totalDomains: number;
  totalScans24h: number;
  averageHealthScore: number;
  adoptionBreakdown: { featureName: string; activeOrgPercent: number }[];
  customers: CustomerMetric[];
  revenueMetrics?: RevenueMetrics;
}

export function calculateRevenueMetrics(
  customers: CustomerMetric[],
): RevenueMetrics {
  const tierCounts: Record<
    "FREE" | "STARTER" | "AGENCY_PRO" | "ENTERPRISE",
    { subscribers: number; paying: number; mrrCents: number }
  > = {
    FREE: { subscribers: 0, paying: 0, mrrCents: 0 },
    STARTER: { subscribers: 0, paying: 0, mrrCents: 0 },
    AGENCY_PRO: { subscribers: 0, paying: 0, mrrCents: 0 },
    ENTERPRISE: { subscribers: 0, paying: 0, mrrCents: 0 },
  };

  let totalMrrCents = 0;
  let activePayingAccounts = 0;
  let totalSubscribers = 0;

  for (const c of customers) {
    const tier = c.plan;
    if (!tierCounts[tier]) continue;

    totalSubscribers++;
    tierCounts[tier].subscribers++;

    // Domain rules: Only ACTIVE paid subscriptions contribute to MRR (TRIAL, FREE, CANCELED contribute $0)
    const isActive = c.subscriptionStatus === "ACTIVE";
    const isPaidTier = tier !== "FREE";

    if (isActive && isPaidTier) {
      activePayingAccounts++;
      tierCounts[tier].paying++;

      const pricing = AUTHORITATIVE_PLAN_PRICING[tier];
      const isAnnual = c.billingCadence === "ANNUAL";
      const mrrContribution = isAnnual
        ? pricing.annualMonthlyPriceCents
        : pricing.monthlyPriceCents;

      totalMrrCents += mrrContribution;
      tierCounts[tier].mrrCents += mrrContribution;
    }
  }

  const arrRunRateCents = totalMrrCents * 12;
  const arpuCents =
    activePayingAccounts > 0
      ? Math.round(totalMrrCents / activePayingAccounts)
      : 0;

  const tiers: ("FREE" | "STARTER" | "AGENCY_PRO" | "ENTERPRISE")[] = [
    "ENTERPRISE",
    "AGENCY_PRO",
    "STARTER",
    "FREE",
  ];
  const tierLabels: Record<
    "FREE" | "STARTER" | "AGENCY_PRO" | "ENTERPRISE",
    string
  > = {
    ENTERPRISE: "Enterprise ($999/mo | $799/mo ann)",
    AGENCY_PRO: "Agency Pro ($199/mo | $159/mo ann)",
    STARTER: "Starter ($49/mo | $39/mo ann)",
    FREE: "Free ($0)",
  };

  const tierDistribution: PlanRevenueDistribution[] = tiers.map((t) => ({
    tier: t,
    tierName: tierLabels[t],
    subscriberCount: tierCounts[t].subscribers,
    payingCount: tierCounts[t].paying,
    mrrCents: tierCounts[t].mrrCents,
    percentageOfMrr:
      totalMrrCents > 0
        ? Math.round((tierCounts[t].mrrCents / totalMrrCents) * 100)
        : 0,
  }));

  return {
    totalMrrCents,
    arrRunRateCents,
    activePayingAccounts,
    totalSubscribers,
    arpuCents,
    tierDistribution,
  };
}

export const AdminControlPlane: React.FC<AdminControlPlaneProps> = ({
  totalCustomers,
  totalDomains,
  totalScans24h,
  averageHealthScore,
  adoptionBreakdown,
  customers,
  revenueMetrics: explicitRevenueMetrics,
}) => {
  // Derive revenue metrics strictly from customer subscription data if not explicitly provided
  const revenue = explicitRevenueMetrics || calculateRevenueMetrics(customers);

  const getChurnBadge = (risk: "LOW" | "MEDIUM" | "HIGH") => {
    switch (risk) {
      case "HIGH":
        return "bg-rose-100 text-rose-800 border-rose-200";
      case "MEDIUM":
        return "bg-amber-100 text-amber-800 border-amber-200";
      case "LOW":
        return "bg-emerald-100 text-emerald-800 border-emerald-200";
    }
  };

  const formatCurrency = (cents: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      maximumFractionDigits: 0,
    }).format(cents / 100);
  };

  return (
    <div className="space-y-8">
      {/* 1. Global Platform Metrics (ADMIN-001) */}
      <section
        aria-labelledby="global-metrics-title"
        className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
      >
        <h2
          id="global-metrics-title"
          className="text-base font-bold text-slate-900 mb-4"
        >
          Internal Control Plane Metrics
        </h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">
              Total Customers
            </span>
            <span className="text-2xl font-bold text-slate-900">
              {totalCustomers}
            </span>
          </div>
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">
              Monitored Domains
            </span>
            <span className="text-2xl font-bold text-slate-900">
              {totalDomains}
            </span>
          </div>
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">
              24h Scans Executed
            </span>
            <span className="text-2xl font-bold text-slate-900">
              {totalScans24h}
            </span>
          </div>
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">
              Avg Platform Health
            </span>
            <span className="text-2xl font-bold text-emerald-600">
              {averageHealthScore}%
            </span>
          </div>
        </div>
      </section>

      {/* 2. Revenue & MRR Tracker (ADMIN-004) */}
      <section
        aria-labelledby="revenue-metrics-title"
        className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm space-y-6"
      >
        <div className="flex items-center justify-between border-b pb-3 border-slate-100">
          <div>
            <h2
              id="revenue-metrics-title"
              className="text-base font-bold text-slate-900"
            >
              Subscription Revenue & MRR Tracker (ADMIN-004)
            </h2>
            <p className="text-xs text-slate-500">
              Deterministic calculations derived directly from active customer
              subscriptions and PlanDefinition (USD)
            </p>
          </div>
          <span className="text-xs font-mono font-semibold px-2.5 py-1 bg-emerald-50 text-emerald-700 rounded border border-emerald-200">
            {revenue.activePayingAccounts} Active Paid Accounts
          </span>
        </div>

        {/* Core Revenue KPIs */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 bg-emerald-50/50 rounded-lg border border-emerald-100">
            <span className="text-xs text-emerald-700 uppercase block font-bold tracking-wider">
              Monthly Recurring (MRR)
            </span>
            <span className="text-2xl font-bold text-slate-900 mt-1 block">
              {formatCurrency(revenue.totalMrrCents)}
            </span>
            <span className="text-[11px] text-slate-500 mt-0.5 block">
              Sum of active paid accounts
            </span>
          </div>

          <div className="p-4 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-xs text-slate-500 uppercase block font-bold tracking-wider">
              Annual Run-Rate (ARR)
            </span>
            <span className="text-2xl font-bold text-slate-900 mt-1 block">
              {formatCurrency(revenue.arrRunRateCents)}
            </span>
            <span className="text-[11px] text-slate-500 mt-0.5 block">
              MRR × 12 annualized
            </span>
          </div>

          <div className="p-4 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-xs text-slate-500 uppercase block font-bold tracking-wider">
              Avg Rev Per Account (ARPU)
            </span>
            <span className="text-2xl font-bold text-slate-900 mt-1 block">
              {revenue.activePayingAccounts > 0
                ? formatCurrency(revenue.arpuCents)
                : "$0"}
            </span>
            <span className="text-[11px] text-slate-500 mt-0.5 block">
              MRR ÷ active paying accounts
            </span>
          </div>

          <div className="p-4 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-xs text-slate-500 uppercase block font-bold tracking-wider">
              Active Paid Ratio
            </span>
            <span className="text-2xl font-bold text-slate-900 mt-1 block">
              {revenue.totalSubscribers > 0
                ? `${Math.round((revenue.activePayingAccounts / revenue.totalSubscribers) * 100)}%`
                : "0%"}
            </span>
            <span className="text-[11px] text-slate-500 mt-0.5 block">
              {revenue.activePayingAccounts} of {revenue.totalSubscribers}{" "}
              accounts
            </span>
          </div>
        </div>

        {/* Plan Tier Distribution Breakdown */}
        <div>
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700 mb-3">
            Plan-Tier Revenue Contribution & Subscriber Cohorts
          </h3>
          <div className="overflow-x-auto border border-slate-200 rounded-lg">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-[11px] font-semibold text-slate-500 uppercase border-b">
                <tr>
                  <th scope="col" className="px-4 py-2.5">
                    Plan Tier
                  </th>
                  <th scope="col" className="px-3 py-2.5 text-center">
                    Total Accounts
                  </th>
                  <th scope="col" className="px-3 py-2.5 text-center">
                    Active Paid
                  </th>
                  <th scope="col" className="px-4 py-2.5 text-right">
                    MRR Contribution
                  </th>
                  <th scope="col" className="px-4 py-2.5 text-right">
                    % of Total MRR
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono">
                {revenue.tierDistribution.map((t) => (
                  <tr key={t.tier} className="hover:bg-slate-50">
                    <td className="px-4 py-2.5 font-sans font-semibold text-slate-900">
                      {t.tierName}
                    </td>
                    <td className="px-3 py-2.5 text-center">
                      {t.subscriberCount}
                    </td>
                    <td className="px-3 py-2.5 text-center text-emerald-700 font-semibold">
                      {t.payingCount}
                    </td>
                    <td className="px-4 py-2.5 text-right font-semibold text-slate-900">
                      {formatCurrency(t.mrrCents)}
                    </td>
                    <td className="px-4 py-2.5 text-right font-sans">
                      <span className="inline-block px-2 py-0.5 rounded bg-slate-100 text-slate-800 text-[11px] font-semibold">
                        {t.percentageOfMrr}%
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      {/* 3. Feature Adoption Distribution (ADMIN-002) */}
      <section
        aria-labelledby="adoption-dist-title"
        className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
      >
        <h2
          id="adoption-dist-title"
          className="text-base font-bold text-slate-900 mb-4"
        >
          Feature-Adoption Distribution (Cohorts)
        </h2>
        <div className="space-y-3">
          {adoptionBreakdown.map((item) => (
            <div key={item.featureName} className="space-y-1">
              <div className="flex justify-between text-xs font-semibold text-slate-700">
                <span>{item.featureName}</span>
                <span>{item.activeOrgPercent}% of Organizations</span>
              </div>
              <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-slate-900 rounded-full"
                  style={{ width: `${item.activeOrgPercent}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 4. Customer Churn-Risk View (ADMIN-003) */}
      <section
        aria-labelledby="churn-view-title"
        className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
      >
        <h2
          id="churn-view-title"
          className="text-base font-bold text-slate-900 mb-4"
        >
          Customer Accounts & Churn-Risk Tracker
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase border-b">
              <tr>
                <th scope="col" className="px-4 py-3">
                  Organization
                </th>
                <th scope="col" className="px-3 py-3">
                  Plan
                </th>
                <th scope="col" className="px-3 py-3">
                  Billing
                </th>
                <th scope="col" className="px-3 py-3">
                  Status
                </th>
                <th scope="col" className="px-3 py-3">
                  Domains
                </th>
                <th scope="col" className="px-3 py-3">
                  Health
                </th>
                <th scope="col" className="px-3 py-3">
                  Adoption
                </th>
                <th scope="col" className="px-3 py-3">
                  Churn Risk
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Last Active
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {customers.map((c) => (
                <tr key={c.orgId} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-semibold text-slate-900">
                    {c.orgName}
                  </td>
                  <td className="px-3 py-3 text-xs font-mono">{c.plan}</td>
                  <td className="px-3 py-3 text-xs font-mono">
                    {c.billingCadence || "MONTHLY"}
                  </td>
                  <td className="px-3 py-3 text-xs font-mono">
                    <span
                      className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                        c.subscriptionStatus === "ACTIVE"
                          ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                          : c.subscriptionStatus === "TRIAL"
                            ? "bg-blue-50 text-blue-700 border border-blue-200"
                            : "bg-slate-100 text-slate-600"
                      }`}
                    >
                      {c.subscriptionStatus || "ACTIVE"}
                    </span>
                  </td>
                  <td className="px-3 py-3">{c.domainsMonitored}</td>
                  <td className="px-3 py-3 font-bold text-slate-800">
                    {c.healthScore}%
                  </td>
                  <td className="px-3 py-3">{c.adoptionScore}%</td>
                  <td className="px-3 py-3">
                    <span
                      className={`text-xs font-bold px-2 py-0.5 rounded border ${getChurnBadge(c.churnRisk)}`}
                    >
                      {c.churnRisk}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-slate-400 text-right">
                    {c.lastActive}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
};
