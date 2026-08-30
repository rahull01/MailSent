"use client";

import React from "react";

export interface HealthOverviewProps {
  score: number;
  totalDomains: number;
  healthyCount: number;
  atRiskCount: number;
  criticalCount: number;
  trendDelta?: number;
}

export const HealthOverview: React.FC<HealthOverviewProps> = ({
  score,
  totalDomains,
  healthyCount,
  atRiskCount,
  criticalCount,
  trendDelta = 0,
}) => {
  const getScoreColor = (s: number) => {
    if (s >= 80) return "text-emerald-600 bg-emerald-50 border-emerald-200";
    if (s >= 50) return "text-amber-600 bg-amber-50 border-amber-200";
    return "text-rose-600 bg-rose-50 border-rose-200";
  };

  return (
    <section
      aria-labelledby="health-overview-title"
      className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
    >
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2
            id="health-overview-title"
            className="text-lg font-semibold text-slate-900"
          >
            Portfolio Health Overview
          </h2>
          <p className="text-sm text-slate-500">
            Aggregated delivery infrastructure confidence across {totalDomains}{" "}
            domains
          </p>
        </div>
        <div
          className={`flex items-center gap-2 px-3 py-1.5 rounded-full border font-bold text-sm ${getScoreColor(score)}`}
        >
          <span>{score}% Health Score</span>
          {trendDelta !== 0 && (
            <span className="text-xs font-normal">
              ({trendDelta > 0 ? `+${trendDelta}%` : `${trendDelta}%`})
            </span>
          )}
        </div>
      </div>

      <div className="grid grid-cols-3 gap-4 pt-2">
        <div className="p-4 rounded-lg bg-emerald-50 border border-emerald-100">
          <p className="text-xs font-medium text-emerald-800 uppercase tracking-wider">
            Healthy
          </p>
          <p className="text-2xl font-bold text-emerald-900 mt-1">
            {healthyCount}
          </p>
          <p className="text-xs text-emerald-700 mt-0.5">
            Inbox delivery optimal
          </p>
        </div>
        <div className="p-4 rounded-lg bg-amber-50 border border-amber-100">
          <p className="text-xs font-medium text-amber-800 uppercase tracking-wider">
            At Risk
          </p>
          <p className="text-2xl font-bold text-amber-900 mt-1">
            {atRiskCount}
          </p>
          <p className="text-xs text-amber-700 mt-0.5">Warnings detected</p>
        </div>
        <div className="p-4 rounded-lg bg-rose-50 border border-rose-100">
          <p className="text-xs font-medium text-rose-800 uppercase tracking-wider">
            Critical
          </p>
          <p className="text-2xl font-bold text-rose-900 mt-1">
            {criticalCount}
          </p>
          <p className="text-xs text-rose-700 mt-0.5">
            Delivery failure active
          </p>
        </div>
      </div>
    </section>
  );
};
