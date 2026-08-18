'use client';

import React from 'react';

export interface FiveQuestionsData {
  whatHappened: string;
  why: string;
  businessImpact: string;
  whatToDo: string;
  whatIfIgnored: string;
  evidenceSnapshotId?: string;
}

export interface FiveQuestionsCardProps {
  data: FiveQuestionsData;
  title?: string;
  severity?: 'CRITICAL' | 'WARNING' | 'INFO';
  onActionClick?: () => void;
  actionLabel?: string;
}

export const FiveQuestionsCard: React.FC<FiveQuestionsCardProps> = ({
  data,
  title = 'Diagnosis & Guidance',
  severity = 'WARNING',
  onActionClick,
  actionLabel = 'Apply Recommended Fix',
}) => {
  const getBorderColor = () => {
    switch (severity) {
      case 'CRITICAL': return 'border-rose-300 bg-rose-50/30';
      case 'WARNING': return 'border-amber-300 bg-amber-50/30';
      default: return 'border-slate-200 bg-slate-50/30';
    }
  };

  return (
    <article 
      aria-labelledby="five-questions-title"
      className={`p-6 bg-white rounded-xl border-2 shadow-sm ${getBorderColor()} space-y-4`}
    >
      <div className="flex items-center justify-between border-b pb-3 border-slate-100">
        <h3 id="five-questions-title" className="text-base font-bold text-slate-900">
          {title}
        </h3>
        {data.evidenceSnapshotId && (
          <span className="text-xs font-mono text-slate-400">
            Lineage: #{data.evidenceSnapshotId}
          </span>
        )}
      </div>

      <div className="space-y-3 text-sm">
        {/* 1. What happened? */}
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
            1. What happened?
          </h4>
          <p className="font-semibold text-slate-900 mt-0.5">{data.whatHappened}</p>
        </div>

        {/* 2. Why? */}
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
            2. Why?
          </h4>
          <p className="text-slate-700 mt-0.5">{data.why}</p>
        </div>

        {/* 3. What is the business impact? */}
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
            3. Business Impact
          </h4>
          <p className="text-rose-700 font-medium mt-0.5">{data.businessImpact}</p>
        </div>

        {/* 4. What should I do? */}
        <div className="p-3 bg-emerald-50 rounded-lg border border-emerald-200">
          <h4 className="text-xs font-bold uppercase tracking-wider text-emerald-800">
            4. Recommended Action
          </h4>
          <p className="text-emerald-950 font-medium mt-0.5">{data.whatToDo}</p>
          {onActionClick && (
            <button
              type="button"
              onClick={onActionClick}
              className="mt-2 text-xs font-bold px-3 py-1.5 bg-emerald-700 text-white hover:bg-emerald-800 rounded shadow-sm transition-colors focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            >
              {actionLabel}
            </button>
          )}
        </div>

        {/* 5. What happens if I ignore it? */}
        <div className="p-3 bg-slate-100 rounded-lg border border-slate-200">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-600">
            5. What happens if you ignore this?
          </h4>
          <p className="text-slate-700 text-xs mt-0.5">{data.whatIfIgnored}</p>
        </div>
      </div>
    </article>
  );
};
