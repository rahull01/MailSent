"use client";

import React from "react";

export interface ActionQueueItem {
  id: string;
  domain: string;
  actionTitle: string;
  expectedHealthGain: string;
  confidenceScore: number;
  isExecuting?: boolean;
}

export interface ActionQueueWidgetProps {
  actions: ActionQueueItem[];
  onAuthorizeAction?: (id: string) => void;
}

export const ActionQueueWidget: React.FC<ActionQueueWidgetProps> = ({
  actions,
  onAuthorizeAction,
}) => {
  return (
    <section
      aria-labelledby="action-queue-title"
      className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
    >
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2
            id="action-queue-title"
            className="text-lg font-semibold text-slate-900"
          >
            Action Queue (Consent-Gated)
          </h2>
          <p className="text-sm text-slate-500">
            Approved fixes ready for execution with human-in-the-loop consent
          </p>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 bg-amber-50 text-amber-800 rounded-full border border-amber-200">
          {actions.length} Actions Pending
        </span>
      </div>

      {actions.length === 0 ? (
        <div className="text-center py-6 text-slate-500 text-sm">
          No actions requiring consent right now.
        </div>
      ) : (
        <div className="space-y-3">
          {actions.map((act) => (
            <div
              key={act.id}
              className="p-4 rounded-lg border border-slate-200 bg-slate-50 flex items-center justify-between gap-4"
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono font-medium text-slate-600 bg-white px-2 py-0.5 rounded border">
                    {act.domain}
                  </span>
                  <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                    +{act.expectedHealthGain}
                  </span>
                  <span className="text-xs text-slate-400">
                    AI Confidence: {act.confidenceScore}%
                  </span>
                </div>
                <h3 className="text-sm font-semibold text-slate-900">
                  {act.actionTitle}
                </h3>
              </div>
              <button
                type="button"
                disabled={act.isExecuting}
                onClick={() => onAuthorizeAction?.(act.id)}
                className="shrink-0 text-xs font-bold px-4 py-2 bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-50 rounded-lg shadow-sm transition-colors focus:ring-2 focus:ring-emerald-500 focus:outline-none"
              >
                {act.isExecuting ? "Executing..." : "Authorize & Execute"}
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  );
};
