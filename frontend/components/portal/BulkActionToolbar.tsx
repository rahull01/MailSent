'use client';

import React from 'react';

export interface BulkActionToolbarProps {
  selectedCount: number;
  onBulkScan?: () => void;
  onBulkTag?: (tag: string) => void;
  onBulkArchive?: () => void;
  onClearSelection?: () => void;
}

export const BulkActionToolbar: React.FC<BulkActionToolbarProps> = ({
  selectedCount,
  onBulkScan,
  onBulkTag,
  onBulkArchive,
  onClearSelection,
}) => {
  if (selectedCount === 0) return null;

  return (
    <div 
      role="toolbar" 
      aria-label="Bulk actions toolbar"
      className="fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-slate-900 text-white px-6 py-3 rounded-full shadow-2xl flex items-center gap-4 z-40 border border-slate-700 animate-in fade-in slide-in-from-bottom-4 duration-200"
    >
      <div className="flex items-center gap-2 pr-3 border-r border-slate-700">
        <span className="text-xs font-bold bg-white text-slate-900 rounded-full w-5 h-5 flex items-center justify-center">
          {selectedCount}
        </span>
        <span className="text-xs font-medium text-slate-300">Selected</span>
      </div>

      <div className="flex items-center gap-2">
        {onBulkScan && (
          <button
            type="button"
            onClick={onBulkScan}
            className="text-xs font-semibold px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-full transition-colors"
          >
            ⚡ Bulk Rescan
          </button>
        )}

        {onBulkTag && (
          <button
            type="button"
            onClick={() => {
              const tag = prompt('Enter tag name to apply to selected:');
              if (tag) onBulkTag(tag);
            }}
            className="text-xs font-semibold px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-full transition-colors border border-slate-700"
          >
            🏷️ Add Tag
          </button>
        )}

        {onBulkArchive && (
          <button
            type="button"
            onClick={onBulkArchive}
            className="text-xs font-semibold px-3 py-1.5 bg-slate-800 hover:bg-rose-900 text-slate-200 hover:text-rose-200 rounded-full transition-colors border border-slate-700"
          >
            📦 Archive
          </button>
        )}
      </div>

      {onClearSelection && (
        <button
          type="button"
          onClick={onClearSelection}
          className="text-xs text-slate-400 hover:text-white pl-2 underline"
        >
          Cancel
        </button>
      )}
    </div>
  );
};
