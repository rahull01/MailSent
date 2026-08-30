"use client";

import React from "react";

export interface PrioritizedIssueItem {
  id: string;
  domain: string;
  title: string;
  businessImpact: string;
  priorityScore: number; // 0-100
  urgency: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";
  onResolve?: (id: string) => void;
}

export interface IssueQueueProps {
  issues: PrioritizedIssueItem[];
  onSelectIssue?: (id: string) => void;
}

export const IssueQueue: React.FC<IssueQueueProps> = ({
  issues,
  onSelectIssue,
}) => {
  // Sort descending by priorityScore
  const sortedIssues = [...issues].sort(
    (a, b) => b.priorityScore - a.priorityScore,
  );

  const getUrgencyBadge = (urgency: PrioritizedIssueItem["urgency"]) => {
    switch (urgency) {
      case "CRITICAL":
        return "bg-rose-100 text-rose-800 border-rose-200";
      case "HIGH":
        return "bg-amber-100 text-amber-800 border-amber-200";
      case "MEDIUM":
        return "bg-blue-100 text-blue-800 border-blue-200";
      default:
        return "bg-slate-100 text-slate-800 border-slate-200";
    }
  };

  return (
    <section
      aria-labelledby="issue-queue-title"
      className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
    >
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2
            id="issue-queue-title"
            className="text-lg font-semibold text-slate-900"
          >
            Prioritized Issue Queue
          </h2>
          <p className="text-sm text-slate-500">
            Issues ranked by business risk and immediate deliverability impact
          </p>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 bg-slate-100 text-slate-700 rounded-full border">
          {sortedIssues.length} Active Issues
        </span>
      </div>

      {sortedIssues.length === 0 ? (
        <div className="text-center py-8 text-slate-500">
          <p className="text-sm font-medium text-slate-600">
            No active issues detected.
          </p>
          <p className="text-xs mt-1">
            All monitored domains are healthy and compliant.
          </p>
        </div>
      ) : (
        <ul role="list" className="divide-y divide-slate-100">
          {sortedIssues.map((issue) => (
            <li
              key={issue.id}
              className="py-4 flex items-start justify-between gap-4 hover:bg-slate-50 p-3 rounded-lg transition-colors"
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span
                    className={`text-xs font-bold px-2 py-0.5 rounded border ${getUrgencyBadge(issue.urgency)}`}
                  >
                    {issue.urgency} ({issue.priorityScore})
                  </span>
                  <span className="text-xs font-mono text-slate-500">
                    {issue.domain}
                  </span>
                </div>
                <h3 className="text-sm font-semibold text-slate-900">
                  {issue.title}
                </h3>
                <p className="text-xs text-slate-600">
                  <strong className="text-slate-700">Impact:</strong>{" "}
                  {issue.businessImpact}
                </p>
              </div>
              <button
                type="button"
                onClick={() =>
                  onSelectIssue
                    ? onSelectIssue(issue.id)
                    : issue.onResolve?.(issue.id)
                }
                className="shrink-0 text-xs font-medium px-3 py-1.5 bg-slate-900 text-white hover:bg-slate-800 rounded-md transition-colors focus:ring-2 focus:ring-slate-900 focus:outline-none"
              >
                Inspect & Fix
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
};
