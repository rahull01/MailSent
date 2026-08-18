'use client';

import React from 'react';

export interface CustomerMetric {
  orgId: string;
  orgName: string;
  plan: string;
  domainsMonitored: number;
  healthScore: number;
  adoptionScore: number;
  churnRisk: 'LOW' | 'MEDIUM' | 'HIGH';
  lastActive: string;
}

export interface AdminControlPlaneProps {
  totalCustomers: number;
  totalDomains: number;
  totalScans24h: number;
  averageHealthScore: number;
  adoptionBreakdown: { featureName: string; activeOrgPercent: number }[];
  customers: CustomerMetric[];
}

export const AdminControlPlane: React.FC<AdminControlPlaneProps> = ({
  totalCustomers,
  totalDomains,
  totalScans24h,
  averageHealthScore,
  adoptionBreakdown,
  customers,
}) => {
  const getChurnBadge = (risk: 'LOW' | 'MEDIUM' | 'HIGH') => {
    switch (risk) {
      case 'HIGH': return 'bg-rose-100 text-rose-800 border-rose-200';
      case 'MEDIUM': return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'LOW': return 'bg-emerald-100 text-emerald-800 border-emerald-200';
    }
  };

  return (
    <div className="space-y-8">
      {/* 1. Global Metrics (ADMIN-001) */}
      <section aria-labelledby="global-metrics-title" className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm">
        <h2 id="global-metrics-title" className="text-base font-bold text-slate-900 mb-4">
          Internal Control Plane Metrics
        </h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">Total Customers</span>
            <span className="text-2xl font-bold text-slate-900">{totalCustomers}</span>
          </div>
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">Monitored Domains</span>
            <span className="text-2xl font-bold text-slate-900">{totalDomains}</span>
          </div>
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">24h Scans Executed</span>
            <span className="text-2xl font-bold text-slate-900">{totalScans24h}</span>
          </div>
          <div className="p-4 bg-slate-50 rounded-lg border">
            <span className="text-xs text-slate-500 uppercase block font-medium">Avg Platform Health</span>
            <span className="text-2xl font-bold text-emerald-600">{averageHealthScore}%</span>
          </div>
        </div>
      </section>

      {/* 2. Feature Adoption Distribution (ADMIN-002) */}
      <section aria-labelledby="adoption-dist-title" className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm">
        <h2 id="adoption-dist-title" className="text-base font-bold text-slate-900 mb-4">
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

      {/* 3. Customer Churn-Risk View (ADMIN-003) */}
      <section aria-labelledby="churn-view-title" className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm">
        <h2 id="churn-view-title" className="text-base font-bold text-slate-900 mb-4">
          Customer Accounts & Churn-Risk Tracker
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase border-b">
              <tr>
                <th scope="col" className="px-4 py-3">Organization</th>
                <th scope="col" className="px-3 py-3">Plan</th>
                <th scope="col" className="px-3 py-3">Domains</th>
                <th scope="col" className="px-3 py-3">Health Score</th>
                <th scope="col" className="px-3 py-3">Adoption</th>
                <th scope="col" className="px-3 py-3">Churn Risk</th>
                <th scope="col" className="px-4 py-3 text-right">Last Active</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {customers.map((c) => (
                <tr key={c.orgId} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-semibold text-slate-900">{c.orgName}</td>
                  <td className="px-3 py-3 text-xs font-mono">{c.plan}</td>
                  <td className="px-3 py-3">{c.domainsMonitored}</td>
                  <td className="px-3 py-3 font-bold text-slate-800">{c.healthScore}%</td>
                  <td className="px-3 py-3">{c.adoptionScore}%</td>
                  <td className="px-3 py-3">
                    <span className={`text-xs font-bold px-2 py-0.5 rounded border ${getChurnBadge(c.churnRisk)}`}>
                      {c.churnRisk}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-slate-400 text-right">{c.lastActive}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
};
