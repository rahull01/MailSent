'use client';

import React from 'react';
import { MonitoredDomain } from '../dashboard/MonitoringList';

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

export interface ClientDrilldownProps {
  client: ClientDetails;
  onBack?: () => void;
  onAddDomain?: () => void;
  onExportReport?: () => void;
}

export const ClientDrilldown: React.FC<ClientDrilldownProps> = ({
  client,
  onBack,
  onAddDomain,
  onExportReport,
}) => {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-6">
      <div className="flex items-center justify-between border-b pb-4 border-slate-100">
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
            <p className="text-xs text-slate-500">{client.contactEmail} • Managed since {client.createdAt}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {onExportReport && (
            <button
              type="button"
              onClick={onExportReport}
              className="text-xs font-medium px-3 py-1.5 border border-slate-300 rounded hover:bg-slate-50"
            >
              Export Client PDF
            </button>
          )}
          {onAddDomain && (
            <button
              type="button"
              onClick={onAddDomain}
              className="text-xs font-bold px-3 py-1.5 bg-slate-900 text-white rounded hover:bg-slate-800"
            >
              + Add Domain
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
          <span className="text-xs text-slate-500 uppercase tracking-wider block">Aggregate Health</span>
          <span className="text-2xl font-bold text-slate-900">{client.healthScore}%</span>
        </div>
        <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
          <span className="text-xs text-slate-500 uppercase tracking-wider block">Domains Monitored</span>
          <span className="text-2xl font-bold text-slate-900">{client.domains.length}</span>
        </div>
        <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
          <span className="text-xs text-slate-500 uppercase tracking-wider block">Client Seats / Viewers</span>
          <span className="text-2xl font-bold text-slate-900">{client.viewerCount} Active</span>
        </div>
      </div>

      <div>
        <h3 className="text-sm font-bold text-slate-900 mb-3">Client Domain Inventory</h3>
        <div className="divide-y divide-slate-100 border border-slate-200 rounded-lg overflow-hidden">
          {client.domains.map((dom) => (
            <div key={dom.id} className="p-4 flex items-center justify-between hover:bg-slate-50">
              <div>
                <span className="font-semibold text-sm text-slate-900">{dom.domainName}</span>
                <span className="text-xs text-slate-400 block">Cadence: {dom.cadence} • Last: {dom.lastChecked}</span>
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
      </div>
    </div>
  );
};
