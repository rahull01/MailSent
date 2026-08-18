'use client';

import React, { useState } from 'react';
import { HealthOverview } from '../../components/dashboard/HealthOverview';
import { IssueQueue, PrioritizedIssueItem } from '../../components/dashboard/IssueQueue';
import { MonitoringList, MonitoredDomain } from '../../components/dashboard/MonitoringList';
import { ActionQueueWidget, ActionQueueItem } from '../../components/dashboard/ActionQueueWidget';
import { FiveQuestionsCard, FiveQuestionsData } from '../../components/dashboard/FiveQuestionsCard';

export default function DashboardPage() {
  const [selectedIssue, setSelectedIssue] = useState<FiveQuestionsData | null>({
    whatHappened: 'SPF validation failing intermittently for marketing subdomains.',
    why: 'Too many DNS lookups caused SPF limit (10 lookups) to be exceeded when third-party ESP was included.',
    businessImpact: 'Up to 22% of outbound promotional emails are landing in Gmail spam folders.',
    whatToDo: 'Flatten SPF record to replace nested includes with direct ip4 ranges.',
    whatIfIgnored: 'Inbox delivery reputation will steadily degrade across all Google and Yahoo recipients.',
    evidenceSnapshotId: 'snap-spf-9817',
  });

  const sampleIssues: PrioritizedIssueItem[] = [
    {
      id: 'iss-1',
      domain: 'acme-corp.com',
      title: 'SPF Record lookup limit exceeded (12 / 10 max)',
      businessImpact: 'Promotional campaigns routing directly to spam',
      priorityScore: 92,
      urgency: 'CRITICAL',
    },
    {
      id: 'iss-2',
      domain: 'billing.acme-corp.com',
      title: 'DMARC policy at p=none instead of p=quarantine',
      businessImpact: 'High risk of domain spoofing for invoice delivery',
      priorityScore: 78,
      urgency: 'HIGH',
    },
  ];

  const sampleDomains: MonitoredDomain[] = [
    {
      id: 'dom-1',
      domainName: 'acme-corp.com',
      clientName: 'Acme Enterprise',
      spfStatus: 'WARN',
      dkimStatus: 'PASS',
      dmarcStatus: 'PASS',
      bimiStatus: 'WARN',
      cadence: 'Every 15m',
      lastChecked: '2 mins ago',
    },
    {
      id: 'dom-2',
      domainName: 'billing.acme-corp.com',
      clientName: 'Acme Enterprise',
      spfStatus: 'PASS',
      dkimStatus: 'PASS',
      dmarcStatus: 'WARN',
      bimiStatus: 'FAIL',
      cadence: 'Hourly',
      lastChecked: '12 mins ago',
    },
  ];

  const sampleActions: ActionQueueItem[] = [
    {
      id: 'act-1',
      domain: 'acme-corp.com',
      actionTitle: 'Publish flattened SPF record (v=spf1 ip4:198.51.100.0/24 include:_spf.google.com ~all)',
      expectedHealthGain: '18% Health',
      confidenceScore: 99,
    },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      {/* Accessibility Skip Link */}
      <a 
        href="#main-content" 
        className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-slate-900 focus:text-white focus:rounded"
      >
        Skip to main content
      </a>

      <header role="banner" className="bg-white border-b border-slate-200 px-8 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-slate-900 flex items-center justify-center text-white font-bold text-sm">
              M
            </div>
            <div>
              <h1 className="text-xl font-bold text-slate-900 leading-tight">MailSent Control Hub</h1>
              <p className="text-xs text-slate-500">AI Email Infrastructure Intelligence Platform</p>
            </div>
          </div>
          <div className="flex items-center gap-3 text-xs">
            <span className="inline-flex items-center px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 font-medium border border-emerald-200">
              ● All Systems Operational
            </span>
          </div>
        </div>
      </header>

      <main id="main-content" tabIndex={-1} className="max-w-7xl mx-auto px-8 py-8 space-y-8 focus:outline-none">
        {/* Health Overview */}
        <HealthOverview
          score={84}
          totalDomains={24}
          healthyCount={19}
          atRiskCount={4}
          criticalCount={1}
          trendDelta={+6}
        />

        {/* Five Questions Diagnostic Spotlight (if selected) */}
        {selectedIssue && (
          <section aria-labelledby="active-diagnosis-title">
            <h2 id="active-diagnosis-title" className="sr-only">Active Issue Diagnosis</h2>
            <FiveQuestionsCard
              title="Active Issue Spotlight: acme-corp.com"
              severity="CRITICAL"
              data={selectedIssue}
              actionLabel="Authorize & Execute SPF Flattening"
              onActionClick={() => alert('Consent granted. Automated DNS resolution initiated.')}
            />
          </section>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Issue Queue */}
          <IssueQueue
            issues={sampleIssues}
            onSelectIssue={(id) => {
              // Switch active five questions spotlight
              setSelectedIssue({
                whatHappened: `Inspecting issue #${id}`,
                why: 'Misconfiguration detected in DNS records during continuous background observation.',
                businessImpact: 'Deliverability risk flagged against Google & Yahoo spam rate thresholds.',
                whatToDo: 'Update DNS record to adhere to mailbox provider specifications.',
                whatIfIgnored: 'Permanent reputation penalty applied to sender domain.',
                evidenceSnapshotId: `snap-${id}`,
              });
            }}
          />

          {/* Action Queue */}
          <ActionQueueWidget
            actions={sampleActions}
            onAuthorizeAction={(id) => alert(`Action #${id} authorized and dispatched to consent lane.`)}
          />
        </div>

        {/* Monitoring List */}
        <MonitoringList
          domains={sampleDomains}
          onRescan={(domId) => alert(`Rescan triggered for domain #${domId}`)}
        />
      </main>
    </div>
  );
}
