'use client';

import React from 'react';

export interface OrganizationContext {
  id: string;
  name: string;
  plan: 'AGENCY_PRO' | 'ENTERPRISE' | 'GROWTH';
  isCurrent: boolean;
}

export interface OrgSwitcherProps {
  organizations: OrganizationContext[];
  onSwitchOrg?: (orgId: string) => void;
}

export const OrgSwitcher: React.FC<OrgSwitcherProps> = ({
  organizations,
  onSwitchOrg,
}) => {
  const currentOrg = organizations.find((o) => o.isCurrent) || organizations[0];

  return (
    <div className="relative inline-block text-left">
      <label htmlFor="org-select" className="sr-only">Switch Active Organization</label>
      <select
        id="org-select"
        value={currentOrg?.id}
        onChange={(e) => onSwitchOrg?.(e.target.value)}
        className="text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-900 border border-slate-300 rounded-lg px-3 py-1.5 focus:ring-2 focus:ring-slate-900 focus:outline-none cursor-pointer"
      >
        {organizations.map((org) => (
          <option key={org.id} value={org.id}>
            🏢 {org.name} ({org.plan})
          </option>
        ))}
      </select>
    </div>
  );
};
