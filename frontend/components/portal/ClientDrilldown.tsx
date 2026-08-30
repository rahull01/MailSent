"use client";

import React, { useState } from "react";
import { MonitoredDomain } from "../dashboard/MonitoringList";
import { WhiteLabelConfigState } from "./WhiteLabelConfigModal";

export interface ClientDetails {
  id: string;
  name: string;
  contactEmail: string;
  healthScore: number;
  tags: string[];
  domains: MonitoredDomain[];
  viewerCount: number;
  createdAt: string;
}

export interface ExportStatusState {
  status: "idle" | "loading" | "success" | "error";
  targetDomain?: string;
  message?: string;
}

export interface ClientDrilldownProps {
  client: ClientDetails;
  onBack?: () => void;
  onAddDomain?: () => void;
  onConfigureBranding?: () => void;
  whiteLabelConfig?: WhiteLabelConfigState;
  onExportReport?: (
    client: ClientDetails,
    domain: MonitoredDomain,
  ) => Promise<void> | void;
}

export const ClientDrilldown: React.FC<ClientDrilldownProps> = ({
  client,
  onBack,
  onAddDomain,
  onConfigureBranding,
  whiteLabelConfig,
  onExportReport,
}) => {
  const [selectedDomainId, setSelectedDomainId] = useState<string>(
    client.domains.length > 0 ? client.domains[0].id : "",
  );
  const [exportState, setExportState] = useState<ExportStatusState>({
    status: "idle",
  });

  const selectedDomain =
    client.domains.find((d) => d.id === selectedDomainId) || client.domains[0];
  const isCustomBrandingActive = Boolean(
    whiteLabelConfig && whiteLabelConfig.agencyName !== "MailSent",
  );

  const handleExport = async () => {
    if (!selectedDomain) {
      setExportState({
        status: "error",
        message:
          "No domain selected or domain inventory is empty for this client.",
      });
      return;
    }

    setExportState({
      status: "loading",
      targetDomain: selectedDomain.domainName,
      message: `Generating compliance PDF for ${selectedDomain.domainName}...`,
    });

    try {
      if (onExportReport) {
        await onExportReport(client, selectedDomain);
      }
      setExportState({
        status: "success",
        targetDomain: selectedDomain.domainName,
        message: `Successfully exported compliance report for ${selectedDomain.domainName}`,
      });
    } catch (err: unknown) {
      const errMsg =
        err instanceof Error ? err.message : "Export request failed";
      setExportState({
        status: "error",
        targetDomain: selectedDomain.domainName,
        message: `Export failed for ${selectedDomain.domainName}: ${errMsg}`,
      });
    }
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-6">
      {/* Header & Primary Actions */}
      <div className="flex flex-col md:flex-row md:items-center justify-between border-b pb-4 border-slate-100 gap-4">
        <div className="flex items-center gap-3">
          {onBack && (
            <button
              type="button"
              onClick={onBack}
              className="text-xs font-semibold text-slate-500 hover:text-slate-900 px-2.5 py-1 bg-slate-100 rounded"
            >
              ← Back to Clients
            </button>
          )}
          <div>
            <h2 className="text-xl font-bold text-slate-900">{client.name}</h2>
            <p className="text-xs text-slate-500">
              {client.contactEmail} • Managed since {client.createdAt}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {onConfigureBranding && (
            <button
              type="button"
              onClick={onConfigureBranding}
              className="text-xs font-semibold px-3 py-1.5 border border-slate-300 rounded-lg hover:bg-slate-50 text-slate-700 flex items-center gap-1.5"
            >
              <span>🎨</span>
              <span>Branding Config</span>
              {isCustomBrandingActive && (
                <span
                  className="inline-block w-2 h-2 rounded-full bg-emerald-500"
                  title="Custom white-label active"
                />
              )}
            </button>
          )}

          {client.domains.length > 0 && (
            <div className="flex items-center gap-1.5">
              <label htmlFor="export-domain-select" className="sr-only">
                Select domain to export
              </label>
              <select
                id="export-domain-select"
                value={selectedDomainId}
                onChange={(e) => setSelectedDomainId(e.target.value)}
                className="text-xs font-semibold bg-slate-50 border border-slate-300 rounded-lg px-2.5 py-1.5 text-slate-800 focus:ring-1 focus:ring-slate-900"
              >
                {client.domains.map((dom) => (
                  <option key={dom.id} value={dom.id}>
                    {dom.domainName}
                  </option>
                ))}
              </select>

              <button
                type="button"
                onClick={handleExport}
                disabled={exportState.status === "loading"}
                className="text-xs font-bold px-3 py-1.5 border border-slate-300 bg-white rounded-lg hover:bg-slate-50 text-slate-900 flex items-center gap-1.5 disabled:opacity-50"
              >
                {exportState.status === "loading" ? (
                  <>
                    <span className="inline-block w-3 h-3 border-2 border-slate-900 border-t-transparent rounded-full animate-spin" />
                    <span>Exporting...</span>
                  </>
                ) : (
                  <>
                    <span>📄</span>
                    <span>Export Client PDF</span>
                  </>
                )}
              </button>
            </div>
          )}

          {onAddDomain && (
            <button
              type="button"
              onClick={onAddDomain}
              className="text-xs font-bold px-3 py-1.5 bg-slate-900 text-white rounded-lg hover:bg-slate-800"
            >
              + Add Domain
            </button>
          )}
        </div>
      </div>

      {/* White-Label Active Indicator & Banner */}
      <div className="p-3.5 bg-slate-50 border border-slate-200/80 rounded-lg flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2.5">
          <span className="font-semibold text-slate-500 uppercase tracking-wider text-[11px]">
            Branding Mode:
          </span>
          {isCustomBrandingActive ? (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-semibold border border-emerald-200">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              White-Label Active ({whiteLabelConfig?.agencyName})
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-slate-200 text-slate-700 font-semibold border border-slate-300">
              Neutral MailSent
            </span>
          )}
        </div>

        {whiteLabelConfig && (
          <div className="flex items-center gap-3 text-slate-600 font-mono text-[11px]">
            <span>
              Accent:{" "}
              <strong className="font-semibold">
                {whiteLabelConfig.accentColor}
              </strong>
            </span>
            {whiteLabelConfig.logoUrl && (
              <span>
                Logo:{" "}
                <strong className="font-semibold">
                  {whiteLabelConfig.logoUrl}
                </strong>
              </span>
            )}
            <span className="text-[10px] text-amber-700 font-sans italic">
              (Session preview only)
            </span>
          </div>
        )}
      </div>

      {/* Export Status Notification Banner */}
      {exportState.status !== "idle" && (
        <div
          role="status"
          className={`p-3 rounded-lg border text-xs flex items-center justify-between ${
            exportState.status === "loading"
              ? "bg-blue-50 border-blue-200 text-blue-800"
              : exportState.status === "success"
                ? "bg-emerald-50 border-emerald-200 text-emerald-800"
                : "bg-rose-50 border-rose-200 text-rose-800"
          }`}
        >
          <div className="flex items-center gap-2">
            <span>
              {exportState.status === "loading"
                ? "⏳"
                : exportState.status === "success"
                  ? "✅"
                  : "❌"}
            </span>
            <span>{exportState.message}</span>
          </div>
          <button
            type="button"
            onClick={() => setExportState({ status: "idle" })}
            className="text-slate-400 hover:text-slate-600 font-bold ml-4"
          >
            ✕
          </button>
        </div>
      )}

      {/* Aggregate Health Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
          <span className="text-xs text-slate-500 uppercase tracking-wider block">
            Aggregate Health
          </span>
          <span className="text-2xl font-bold text-slate-900">
            {client.healthScore}%
          </span>
        </div>
        <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
          <span className="text-xs text-slate-500 uppercase tracking-wider block">
            Domains Monitored
          </span>
          <span className="text-2xl font-bold text-slate-900">
            {client.domains.length}
          </span>
        </div>
        <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
          <span className="text-xs text-slate-500 uppercase tracking-wider block">
            Client Seats / Viewers
          </span>
          <span className="text-2xl font-bold text-slate-900">
            {client.viewerCount} Active
          </span>
        </div>
      </div>

      {/* Domain Inventory */}
      <div>
        <h3 className="text-sm font-bold text-slate-900 mb-3">
          Client Domain Inventory
        </h3>
        {client.domains.length === 0 ? (
          <div className="p-8 border border-dashed border-slate-300 rounded-lg text-center text-slate-500 text-xs">
            No domains currently monitored for this client organization.
          </div>
        ) : (
          <div className="divide-y divide-slate-100 border border-slate-200 rounded-lg overflow-hidden">
            {client.domains.map((dom) => (
              <div
                key={dom.id}
                className="p-4 flex items-center justify-between hover:bg-slate-50"
              >
                <div>
                  <span className="font-semibold text-sm text-slate-900">
                    {dom.domainName}
                  </span>
                  <span className="text-xs text-slate-400 block">
                    Cadence: {dom.cadence} • Last: {dom.lastChecked}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono font-medium px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                    SPF: {dom.spfStatus}
                  </span>
                  <span className="text-xs font-mono font-medium px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                    DKIM: {dom.dkimStatus}
                  </span>
                  <span className="text-xs font-mono font-medium px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                    DMARC: {dom.dmarcStatus}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
