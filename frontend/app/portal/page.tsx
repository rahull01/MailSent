'use client';

import React, { useState } from 'react';
import { MultiClientGrid, ClientSummary } from '../../components/portal/MultiClientGrid';
import { ClientDrilldown, ClientDetails } from '../../components/portal/ClientDrilldown';
import { ClientSeatManager, ClientSeat } from '../../components/portal/ClientSeatManager';
import { BulkActionToolbar } from '../../components/portal/BulkActionToolbar';
import { OrgSwitcher, OrganizationContext } from '../../components/portal/OrgSwitcher';
import { ClientGroupFilter } from '../../components/portal/ClientGroupFilter';

export default function AgencyPortalPage() {
  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  const [selectedTag, setSelectedTag] = useState<string | null>(null);
  const [selectedFolder, setSelectedFolder] = useState<string | null>(null);
  const [selectedClientsCount, setSelectedClientsCount] = useState<number>(0);

  const sampleOrgs: OrganizationContext[] = [
    { id: 'org-1', name: 'Apex Growth Agency', plan: 'AGENCY_PRO', isCurrent: true },
    { id: 'org-2', name: 'Nexus Media Partners', plan: 'ENTERPRISE', isCurrent: false },
  ];

  const sampleClients: ClientSummary[] = [
    {
      id: 'cli-1',
      name: 'Acme Global',
      domainCount: 6,
      healthScore: 88,
      activeIssuesCount: 1,
      tags: ['Enterprise', 'E-Commerce', 'Priority'],
      lastScanAt: '10m ago',
    },
    {
      id: 'cli-2',
      name: 'Beacon Financial',
      domainCount: 4,
      healthScore: 42,
      activeIssuesCount: 3,
      tags: ['FinTech', 'Needs Remediation'],
      lastScanAt: '25m ago',
    },
    {
      id: 'cli-3',
      name: 'Starlight Apparel',
      domainCount: 3,
      healthScore: 95,
      activeIssuesCount: 0,
      tags: ['E-Commerce', 'Retainer'],
      lastScanAt: '1h ago',
    },
  ];

  const sampleClientDetails: ClientDetails = {
    id: 'cli-1',
    name: 'Acme Global',
    contactEmail: 'security@acmeglobal.com',
    healthScore: 88,
    tags: ['Enterprise', 'E-Commerce'],
    viewerCount: 3,
    createdAt: 'March 2026',
    domains: [
      {
        id: 'dom-1',
        domainName: 'acmeglobal.com',
        clientName: 'Acme Global',
        spfStatus: 'PASS',
        dkimStatus: 'PASS',
        dmarcStatus: 'PASS',
        bimiStatus: 'PASS',
        cadence: 'Every 15m',
        lastChecked: '10m ago',
      },
      {
        id: 'dom-2',
        domainName: 'mail.acmeglobal.com',
        clientName: 'Acme Global',
        spfStatus: 'WARN',
        dkimStatus: 'PASS',
        dmarcStatus: 'WARN',
        bimiStatus: 'FAIL',
        cadence: 'Hourly',
        lastChecked: '25m ago',
      },
    ],
  };

  const sampleSeats: ClientSeat[] = [
    {
      id: 'seat-1',
      email: 'alex.cto@acmeglobal.com',
      role: 'CLIENT_ADMIN',
      assignedDomains: ['acmeglobal.com', 'mail.acmeglobal.com'],
      invitedAt: '2026-03-01',
      status: 'ACTIVE',
    },
    {
      id: 'seat-2',
      email: 'marcom@acmeglobal.com',
      role: 'VIEWER',
      assignedDomains: ['acmeglobal.com'],
      invitedAt: '2026-03-15',
      status: 'PENDING',
    },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 pb-20">
      <header className="bg-white border-b border-slate-200 px-8 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h1 className="text-xl font-bold text-slate-900">Agency Command Portal</h1>
            <OrgSwitcher
              organizations={sampleOrgs}
              onSwitchOrg={(id) => alert(`Switched to organization context #${id}`)}
            />
          </div>
          <button
            type="button"
            onClick={() => setSelectedClientsCount((prev) => prev > 0 ? 0 : 3)}
            className="text-xs font-semibold px-3 py-1.5 border rounded-lg bg-slate-100 hover:bg-slate-200"
          >
            {selectedClientsCount > 0 ? 'Deselect All' : 'Select All Clients'}
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-8 py-8 space-y-8">
        {selectedClientId ? (
          <div className="space-y-8">
            <ClientDrilldown
              client={sampleClientDetails}
              onBack={() => setSelectedClientId(null)}
              onAddDomain={() => alert('Add domain dialog opened')}
              onExportReport={() => alert('Exporting branded audit report PDF...')}
            />
            <ClientSeatManager
              clientId={sampleClientDetails.id}
              clientName={sampleClientDetails.name}
              seats={sampleSeats}
              onInviteSeat={(email, role) => alert(`Invited ${email} with role ${role}`)}
              onRevokeSeat={(id) => alert(`Revoked seat #${id}`)}
            />
          </div>
        ) : (
          <div className="space-y-6">
            <ClientGroupFilter
              tags={['Enterprise', 'E-Commerce', 'FinTech', 'Priority', 'Retainer', 'Needs Remediation']}
              selectedTag={selectedTag}
              onSelectTag={setSelectedTag}
              selectedFolder={selectedFolder}
              onSelectFolder={setSelectedFolder}
            />

            <MultiClientGrid
              clients={sampleClients}
              onSelectClient={(id) => setSelectedClientId(id)}
              onBulkScanClient={(id) => alert(`Initiating full domain sweep for client #${id}`)}
            />
          </div>
        )}
      </main>

      <BulkActionToolbar
        selectedCount={selectedClientsCount}
        onBulkScan={() => alert(`Bulk scan triggered for ${selectedClientsCount} clients`)}
        onBulkTag={(tag) => alert(`Applied tag '#${tag}' to ${selectedClientsCount} clients`)}
        onBulkArchive={() => alert(`Archived ${selectedClientsCount} clients`)}
        onClearSelection={() => setSelectedClientsCount(0)}
      />
    </div>
  );
}
