'use client';

import React from 'react';
import { AdminControlPlane, CustomerMetric } from '../../components/admin/AdminControlPlane';

export default function AdminPage() {
  const sampleCustomers: CustomerMetric[] = [
    {
      orgId: 'org-1',
      orgName: 'Apex Growth Agency',
      plan: 'AGENCY_PRO',
      domainsMonitored: 24,
      healthScore: 88,
      adoptionScore: 82,
      churnRisk: 'LOW',
      lastActive: '5m ago',
    },
    {
      orgId: 'org-2',
      orgName: 'Vanguard Media Group',
      plan: 'ENTERPRISE',
      domainsMonitored: 60,
      healthScore: 48,
      adoptionScore: 35,
      churnRisk: 'HIGH',
      lastActive: '3 days ago',
    },
    {
      orgId: 'org-3',
      orgName: 'Beacon Commerce Labs',
      plan: 'GROWTH',
      domainsMonitored: 8,
      healthScore: 94,
      adoptionScore: 90,
      churnRisk: 'LOW',
      lastActive: '12m ago',
    },
  ];

  const sampleAdoption = [
    { featureName: 'Continuous Monitoring (Cadence)', activeOrgPercent: 94 },
    { featureName: 'Automated SPF/DMARC Diagnosis', activeOrgPercent: 88 },
    { featureName: 'AI Explanations & Lineage Tracing', activeOrgPercent: 78 },
    { featureName: 'Consent-Gated Action Queue', activeOrgPercent: 62 },
    { featureName: 'Executive Branded PDF Reports', activeOrgPercent: 54 },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="bg-slate-900 text-white px-8 py-4 border-b border-slate-800">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="bg-rose-600 text-white text-[10px] font-bold px-2 py-0.5 rounded uppercase">
              Admin Ops
            </span>
            <h1 className="text-lg font-bold">MailSent Internal Operations Control Plane</h1>
          </div>
          <span className="text-xs text-slate-400">Environment: Production Internal</span>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-8 py-8">
        <AdminControlPlane
          totalCustomers={142}
          totalDomains={890}
          totalScans24h={14820}
          averageHealthScore={81}
          adoptionBreakdown={sampleAdoption}
          customers={sampleCustomers}
        />
      </main>
    </div>
  );
}
