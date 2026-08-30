"use client";

import React from "react";

export interface ClientGroupFilterProps {
  tags: string[];
  selectedTag: string | null;
  onSelectTag?: (tag: string | null) => void;
  folderGroups?: string[];
  selectedFolder?: string | null;
  onSelectFolder?: (folder: string | null) => void;
}

export const ClientGroupFilter: React.FC<ClientGroupFilterProps> = ({
  tags,
  selectedTag,
  onSelectTag,
  folderGroups = [
    "All Clients",
    "VIP Tier",
    "E-Commerce",
    "SaaS Clients",
    "Needs Remediation",
  ],
  selectedFolder = "All Clients",
  onSelectFolder,
}) => {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4 p-4 bg-slate-50 rounded-xl border border-slate-200">
      {/* Folder Tabs */}
      <div className="flex items-center gap-1.5 overflow-x-auto">
        {folderGroups.map((folder) => (
          <button
            key={folder}
            type="button"
            onClick={() =>
              onSelectFolder?.(folder === "All Clients" ? null : folder)
            }
            className={`text-xs font-semibold px-3 py-1.5 rounded-lg transition-colors whitespace-nowrap ${
              selectedFolder === folder ||
              (!selectedFolder && folder === "All Clients")
                ? "bg-slate-900 text-white shadow-sm"
                : "text-slate-600 hover:bg-slate-200/60"
            }`}
          >
            📁 {folder}
          </button>
        ))}
      </div>

      {/* Tag Badges */}
      <div className="flex items-center gap-1.5">
        <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mr-1">
          Tags:
        </span>
        <button
          type="button"
          onClick={() => onSelectTag?.(null)}
          className={`text-[11px] font-medium px-2.5 py-1 rounded-full border transition-colors ${
            selectedTag === null
              ? "bg-slate-900 text-white border-slate-900"
              : "bg-white text-slate-600 border-slate-200 hover:bg-slate-100"
          }`}
        >
          All
        </button>
        {tags.map((tag) => (
          <button
            key={tag}
            type="button"
            onClick={() => onSelectTag?.(tag === selectedTag ? null : tag)}
            className={`text-[11px] font-medium px-2.5 py-1 rounded-full border transition-colors ${
              selectedTag === tag
                ? "bg-slate-900 text-white border-slate-900"
                : "bg-white text-slate-600 border-slate-200 hover:bg-slate-100"
            }`}
          >
            #{tag}
          </button>
        ))}
      </div>
    </div>
  );
};
