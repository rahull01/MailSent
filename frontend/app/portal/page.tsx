"use client";

import React, { useState } from "react";
import {
  MultiClientGrid,
  ClientSummary,
} from "../../components/portal/MultiClientGrid";
import {
  ClientDrilldown,
  ClientDetails,
} from "../../components/portal/ClientDrilldown";
import {
  ClientSeatManager,
  ClientSeat,
} from "../../components/portal/ClientSeatManager";
import { BulkActionToolbar } from "../../components/portal/BulkActionToolbar";
import {
  OrgSwitcher,
  OrganizationContext,
} from "../../components/portal/OrgSwitcher";
import { ClientGroupFilter } from "../../components/portal/ClientGroupFilter";
import {
  WhiteLabelConfigModal,
  WhiteLabelConfigState,
} from "../../components/portal/WhiteLabelConfigModal";
import { MonitoredDomain } from "../../components/dashboard/MonitoringList";

export default function AgencyPortalPage() {
  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  const [selectedTag, setSelectedTag] = useState<string | null>(null);
  const [selectedFolder, setSelectedFolder] = useState<string | null>(null);
  const [selectedClientsCount, setSelectedClientsCount] = useState<number>(0);
  const [isWhiteLabelModalOpen, setIsWhiteLabelModalOpen] =
    useState<boolean>(false);

  // Agency organizations
  const [organizations, setOrganizations] = useState<OrganizationContext[]>([
    {
      id: "org-1",
      name: "Apex Growth Agency",
      plan: "AGENCY_PRO",
      isCurrent: true,
    },
    {
      id: "org-2",
      name: "Nexus Media Partners",
      plan: "ENTERPRISE",
      isCurrent: false,
    },
  ]);

  const activeOrg = organizations.find((o) => o.isCurrent) || organizations[0];

  // Scoped WhiteLabelConfig state per agencyOrganizationId
  // Default is neutral MailSent branding when unset
  const [agencyBrandingMap, setAgencyBrandingMap] = useState<
    Record<string, WhiteLabelConfigState>
  >({
    "org-1": {
      agencyName: "Apex Growth Agency",
      logoUrl: "https://apex.agency/logo.png",
      accentColor: "#0F172A",
      customDisclaimer:
        "Confidential deliverability assessment prepared for client leadership.",
      customFooter: "Apex Growth Agency Security Operations Center",
    },
    "org-2": {
      agencyName: "Nexus Media Partners",
      logoUrl: "https://nexusmedia.com/branding.png",
      accentColor: "#1E40AF",
      customDisclaimer: "Nexus Enterprise Deliverability & Compliance Audit",
      customFooter: "Nexus Media Deliverability Engineering",
    },
  });

  const activeBranding = agencyBrandingMap[activeOrg.id] || {
    agencyName: "MailSent",
    logoUrl: "",
    accentColor: "#0F172A",
    customDisclaimer: "",
    customFooter: "",
  };

  // Mock Clients scoped per agency
  const agencyClientsMap: Record<string, ClientSummary[]> = {
    "org-1": [
      {
        id: "cli-1",
        name: "Acme Global",
        domainCount: 2,
        healthScore: 88,
        activeIssuesCount: 1,
        tags: ["Enterprise", "E-Commerce", "Priority"],
        lastScanAt: "10m ago",
      },
      {
        id: "cli-2",
        name: "Beacon Financial",
        domainCount: 1,
        healthScore: 42,
        activeIssuesCount: 3,
        tags: ["FinTech", "Needs Remediation"],
        lastScanAt: "25m ago",
      },
      {
        id: "cli-3",
        name: "Starlight Apparel",
        domainCount: 1,
        healthScore: 95,
        activeIssuesCount: 0,
        tags: ["E-Commerce", "Retainer"],
        lastScanAt: "1h ago",
      },
    ],
    "org-2": [
      {
        id: "cli-4",
        name: "Vanguard Media Group",
        domainCount: 3,
        healthScore: 91,
        activeIssuesCount: 0,
        tags: ["Enterprise", "Media"],
        lastScanAt: "5m ago",
      },
    ],
  };

  const currentClients = agencyClientsMap[activeOrg.id] || [];

  const clientDetailsMap: Record<string, ClientDetails> = {
    "cli-1": {
      id: "cli-1",
      name: "Acme Global",
      contactEmail: "security@acmeglobal.com",
      healthScore: 88,
      tags: ["Enterprise", "E-Commerce"],
      viewerCount: 3,
      createdAt: "March 2026",
      domains: [
        {
          id: "dom-1",
          domainName: "acmeglobal.com",
          clientName: "Acme Global",
          spfStatus: "PASS",
          dkimStatus: "PASS",
          dmarcStatus: "PASS",
          bimiStatus: "PASS",
          cadence: "Every 15m",
          lastChecked: "10m ago",
        },
        {
          id: "dom-2",
          domainName: "mail.acmeglobal.com",
          clientName: "Acme Global",
          spfStatus: "WARN",
          dkimStatus: "PASS",
          dmarcStatus: "WARN",
          bimiStatus: "FAIL",
          cadence: "Hourly",
          lastChecked: "25m ago",
        },
      ],
    },
    "cli-2": {
      id: "cli-2",
      name: "Beacon Financial",
      contactEmail: "compliance@beaconfin.com",
      healthScore: 42,
      tags: ["FinTech", "Needs Remediation"],
      viewerCount: 1,
      createdAt: "January 2026",
      domains: [
        {
          id: "dom-3",
          domainName: "beaconfin.com",
          clientName: "Beacon Financial",
          spfStatus: "FAIL",
          dkimStatus: "WARN",
          dmarcStatus: "FAIL",
          bimiStatus: "FAIL",
          cadence: "Hourly",
          lastChecked: "25m ago",
        },
      ],
    },
    "cli-3": {
      id: "cli-3",
      name: "Starlight Apparel",
      contactEmail: "ops@starlight.io",
      healthScore: 95,
      tags: ["E-Commerce", "Retainer"],
      viewerCount: 2,
      createdAt: "February 2026",
      domains: [
        {
          id: "dom-4",
          domainName: "starlight.io",
          clientName: "Starlight Apparel",
          spfStatus: "PASS",
          dkimStatus: "PASS",
          dmarcStatus: "PASS",
          bimiStatus: "WARN",
          cadence: "Every 15m",
          lastChecked: "1h ago",
        },
      ],
    },
    "cli-4": {
      id: "cli-4",
      name: "Vanguard Media Group",
      contactEmail: "admin@vanguardmedia.com",
      healthScore: 91,
      tags: ["Enterprise", "Media"],
      viewerCount: 4,
      createdAt: "April 2026",
      domains: [
        {
          id: "dom-5",
          domainName: "vanguardmedia.com",
          clientName: "Vanguard Media Group",
          spfStatus: "PASS",
          dkimStatus: "PASS",
          dmarcStatus: "PASS",
          bimiStatus: "PASS",
          cadence: "Every 15m",
          lastChecked: "5m ago",
        },
      ],
    },
  };

  const sampleSeats: ClientSeat[] = [
    {
      id: "seat-1",
      email: "alex.cto@acmeglobal.com",
      role: "CLIENT_ADMIN",
      assignedDomains: ["acmeglobal.com", "mail.acmeglobal.com"],
      invitedAt: "2026-03-01",
      status: "ACTIVE",
    },
    {
      id: "seat-2",
      email: "marcom@acmeglobal.com",
      role: "VIEWER",
      assignedDomains: ["acmeglobal.com"],
      invitedAt: "2026-03-15",
      status: "PENDING",
    },
  ];

  // Switch Agency Context handler
  const handleSwitchOrg = (orgId: string) => {
    setOrganizations((prev) =>
      prev.map((o) => ({
        ...o,
        isCurrent: o.id === orgId,
      })),
    );
    // Reset client drilldown selection to prevent cross-tenant UI leak
    setSelectedClientId(null);
    setSelectedClientsCount(0);
    setSelectedTag(null);
    setSelectedFolder(null);
  };

  // Save branding for the current agency session
  const handleSaveBranding = (newBranding: WhiteLabelConfigState) => {
    setAgencyBrandingMap((prev) => ({
      ...prev,
      [activeOrg.id]: newBranding,
    }));
  };

  // Reset current agency branding to MailSent neutral
  const handleResetBranding = () => {
    setAgencyBrandingMap((prev) => ({
      ...prev,
      [activeOrg.id]: {
        agencyName: "MailSent",
        logoUrl: "",
        accentColor: "#0F172A",
        customDisclaimer: "",
        customFooter: "",
      },
    }));
  };

  // Handle deterministic PDF export simulation preserving INV-01, INV-09, INV-11
  const handleExportClientPdf = async (
    client: ClientDetails,
    domain: MonitoredDomain,
  ) => {
    // Verify client belongs to current active agency
    const isAuthorizedClient = currentClients.some((c) => c.id === client.id);
    if (!isAuthorizedClient) {
      throw new Error(
        `Unauthorized: Client organization ${client.id} is not accessible by ${activeOrg.name} (INV-09).`,
      );
    }

    // Verify domain belongs to client
    const isAuthorizedDomain = client.domains.some((d) => d.id === domain.id);
    if (!isAuthorizedDomain) {
      throw new Error(
        `Unauthorized: Domain ${domain.domainName} does not belong to client ${client.name} (INV-09).`,
      );
    }

    // Simulate PDF generation/download latency
    await new Promise((resolve) => setTimeout(resolve, 600));

    // Construct deterministic simulated export payload reference
    const exportRecord = {
      agencyOrgId: activeOrg.id,
      agencyName: activeBranding.agencyName,
      clientOrgId: client.id,
      clientName: client.name,
      domainId: domain.id,
      domainName: domain.domainName,
      accentColor: activeBranding.accentColor,
      disclaimer: activeBranding.customDisclaimer || "Standard MailSent Report",
      exportedAt: new Date().toISOString(),
    };

    // Client-side mock download notification
    console.info(
      "Simulated PDF generated deterministically via CompliancePdfExporter contract:",
      exportRecord,
    );
  };

  const selectedClientDetails = selectedClientId
    ? clientDetailsMap[selectedClientId]
    : null;

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 pb-20">
      <header className="bg-white border-b border-slate-200 px-8 py-4">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-4 flex-wrap">
            <h1 className="text-xl font-bold text-slate-900">
              Agency Command Portal
            </h1>
            <OrgSwitcher
              organizations={organizations}
              onSwitchOrg={handleSwitchOrg}
            />
            <button
              type="button"
              onClick={() => setIsWhiteLabelModalOpen(true)}
              className="text-xs font-semibold px-3 py-1.5 border border-slate-300 rounded-lg hover:bg-slate-50 bg-white text-slate-700 flex items-center gap-1.5 shadow-sm"
            >
              <span>🎨</span>
              <span>
                Branding:{" "}
                <strong className="text-slate-900">
                  {activeBranding.agencyName}
                </strong>
              </span>
            </button>
          </div>
          <button
            type="button"
            onClick={() =>
              setSelectedClientsCount((prev) =>
                prev > 0 ? 0 : currentClients.length,
              )
            }
            className="text-xs font-semibold px-3 py-1.5 border rounded-lg bg-slate-100 hover:bg-slate-200"
          >
            {selectedClientsCount > 0 ? "Deselect All" : "Select All Clients"}
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-8 py-8 space-y-8">
        {selectedClientDetails ? (
          <div className="space-y-8">
            <ClientDrilldown
              client={selectedClientDetails}
              onBack={() => setSelectedClientId(null)}
              onAddDomain={() => alert("Add domain dialog opened")}
              onConfigureBranding={() => setIsWhiteLabelModalOpen(true)}
              whiteLabelConfig={activeBranding}
              onExportReport={handleExportClientPdf}
            />
            <ClientSeatManager
              clientId={selectedClientDetails.id}
              clientName={selectedClientDetails.name}
              seats={sampleSeats}
              onInviteSeat={(email, role) =>
                alert(`Invited ${email} with role ${role}`)
              }
              onRevokeSeat={(id) => alert(`Revoked seat #${id}`)}
            />
          </div>
        ) : (
          <div className="space-y-6">
            <ClientGroupFilter
              tags={[
                "Enterprise",
                "E-Commerce",
                "FinTech",
                "Priority",
                "Retainer",
                "Needs Remediation",
                "Media",
              ]}
              selectedTag={selectedTag}
              onSelectTag={setSelectedTag}
              selectedFolder={selectedFolder}
              onSelectFolder={setSelectedFolder}
            />

            <MultiClientGrid
              clients={currentClients}
              onSelectClient={(id) => setSelectedClientId(id)}
              onBulkScanClient={(id) =>
                alert(`Initiating full domain sweep for client #${id}`)
              }
            />
          </div>
        )}
      </main>

      <BulkActionToolbar
        selectedCount={selectedClientsCount}
        onBulkScan={() =>
          alert(`Bulk scan triggered for ${selectedClientsCount} clients`)
        }
        onBulkTag={(tag) =>
          alert(`Applied tag '#${tag}' to ${selectedClientsCount} clients`)
        }
        onBulkArchive={() => alert(`Archived ${selectedClientsCount} clients`)}
        onClearSelection={() => setSelectedClientsCount(0)}
      />

      <WhiteLabelConfigModal
        isOpen={isWhiteLabelModalOpen}
        onClose={() => setIsWhiteLabelModalOpen(false)}
        currentConfig={activeBranding}
        onSave={handleSaveBranding}
        onResetToDefault={handleResetBranding}
        activeOrgName={activeOrg.name}
      />
    </div>
  );
}
