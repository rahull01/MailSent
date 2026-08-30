"use client";

import React from "react";

export interface ClientSummary {
  id: string;
  name: string;
  domainCount: number;
  healthScore: number;
  activeIssuesCount: number;
  tags: string[];
  lastScanAt: string;
}

export interface MultiClientGridProps {
  clients: ClientSummary[];
  onSelectClient?: (clientId: string) => void;
  onBulkScanClient?: (clientId: string) => void;
}

export const MultiClientGrid: React.FC<MultiClientGridProps> = ({
  clients,
  onSelectClient,
  onBulkScanClient,
}) => {
  const getHealthBadge = (score: number) => {
    if (score >= 80)
      return "bg-emerald-100 text-emerald-800 border-emerald-200";
    if (score >= 50) return "bg-amber-100 text-amber-800 border-amber-200";
    return "bg-rose-100 text-rose-800 border-rose-200";
  };

  return (
    <section
      aria-labelledby="client-grid-title"
      className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm space-y-4"
    >
      <div className="flex items-center justify-between">
        <div>
          <h2
            id="client-grid-title"
            className="text-lg font-semibold text-slate-900"
          >
            Agency Client Command Center
          </h2>
          <p className="text-sm text-slate-500">
            Unified multi-tenant portfolio monitoring across {clients.length}{" "}
            active clients
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {clients.map((c) => (
          <article
            key={c.id}
            className="p-5 rounded-lg border border-slate-200 hover:border-slate-300 hover:shadow-md transition-all bg-white flex flex-col justify-between"
          >
            <div className="space-y-3">
              <div className="flex items-start justify-between gap-2">
                <h3 className="font-bold text-slate-900 text-base leading-tight">
                  {c.name}
                </h3>
                <span
                  className={`text-xs font-bold px-2 py-0.5 rounded border ${getHealthBadge(c.healthScore)}`}
                >
                  {c.healthScore}%
                </span>
              </div>

              <div className="flex flex-wrap gap-1">
                {c.tags.map((t) => (
                  <span
                    key={t}
                    className="text-[10px] font-medium px-2 py-0.5 bg-slate-100 text-slate-600 rounded"
                  >
                    {t}
                  </span>
                ))}
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs py-2 border-y border-slate-100">
                <div>
                  <span className="text-slate-400 block">Domains</span>
                  <span className="font-semibold text-slate-700">
                    {c.domainCount} Monitored
                  </span>
                </div>
                <div>
                  <span className="text-slate-400 block">Issues</span>
                  <span
                    className={`font-semibold ${c.activeIssuesCount > 0 ? "text-rose-600" : "text-slate-700"}`}
                  >
                    {c.activeIssuesCount} Active
                  </span>
                </div>
              </div>
            </div>

            <div className="pt-4 flex items-center justify-between gap-2">
              <span className="text-[11px] text-slate-400">
                Scanned {c.lastScanAt}
              </span>
              <div className="flex items-center gap-2">
                {onBulkScanClient && (
                  <button
                    type="button"
                    onClick={() => onBulkScanClient(c.id)}
                    className="text-xs text-slate-600 hover:text-slate-900 px-2 py-1 bg-slate-100 hover:bg-slate-200 rounded"
                  >
                    Scan All
                  </button>
                )}
                {onSelectClient && (
                  <button
                    type="button"
                    onClick={() => onSelectClient(c.id)}
                    className="text-xs font-bold text-white bg-slate-900 hover:bg-slate-800 px-3 py-1 rounded"
                  >
                    Drill Down
                  </button>
                )}
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
};
