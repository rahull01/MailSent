'use client';

import React from 'react';

export interface MonitoredDomain {
  id: string;
  domainName: string;
  clientName?: string;
  spfStatus: 'PASS' | 'FAIL' | 'WARN';
  dkimStatus: 'PASS' | 'FAIL' | 'WARN';
  dmarcStatus: 'PASS' | 'FAIL' | 'WARN';
  bimiStatus: 'PASS' | 'FAIL' | 'WARN';
  cadence: string;
  lastChecked: string;
}

export interface MonitoringListProps {
  domains: MonitoredDomain[];
  onRescan?: (domainId: string) => void;
}

export const MonitoringList: React.FC<MonitoringListProps> = ({ domains, onRescan }) => {
  const getBadgeClass = (status: 'PASS' | 'FAIL' | 'WARN') => {
    switch (status) {
      case 'PASS':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'WARN':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'FAIL':
        return 'bg-rose-100 text-rose-800 border-rose-200';
    }
  };

  return (
    <section 
      aria-labelledby="monitoring-list-title"
      className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm"
    >
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 id="monitoring-list-title" className="text-lg font-semibold text-slate-900">
            Active Domain Monitoring
          </h2>
          <p className="text-sm text-slate-500">
            Continuous DNS and deliverability verification status
          </p>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 bg-slate-100 text-slate-700 rounded-full border">
          {domains.length} Domains Monitored
        </span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-slate-600">
          <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase border-b border-slate-200">
            <tr>
              <th scope="col" className="px-4 py-3">Domain</th>
              <th scope="col" className="px-3 py-3 text-center">SPF</th>
              <th scope="col" className="px-3 py-3 text-center">DKIM</th>
              <th scope="col" className="px-3 py-3 text-center">DMARC</th>
              <th scope="col" className="px-3 py-3 text-center">BIMI</th>
              <th scope="col" className="px-4 py-3">Cadence</th>
              <th scope="col" className="px-4 py-3">Last Checked</th>
              <th scope="col" className="px-4 py-3 text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {domains.map((d) => (
              <tr key={d.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3 font-medium text-slate-900">
                  <div>{d.domainName}</div>
                  {d.clientName && (
                    <div className="text-xs text-slate-400 font-normal">{d.clientName}</div>
                  )}
                </td>
                <td className="px-3 py-3 text-center">
                  <span className={`text-xs font-bold px-2 py-0.5 rounded border ${getBadgeClass(d.spfStatus)}`}>
                    {d.spfStatus}
                  </span>
                </td>
                <td className="px-3 py-3 text-center">
                  <span className={`text-xs font-bold px-2 py-0.5 rounded border ${getBadgeClass(d.dkimStatus)}`}>
                    {d.dkimStatus}
                  </span>
                </td>
                <td className="px-3 py-3 text-center">
                  <span className={`text-xs font-bold px-2 py-0.5 rounded border ${getBadgeClass(d.dmarcStatus)}`}>
                    {d.dmarcStatus}
                  </span>
                </td>
                <td className="px-3 py-3 text-center">
                  <span className={`text-xs font-bold px-2 py-0.5 rounded border ${getBadgeClass(d.bimiStatus)}`}>
                    {d.bimiStatus}
                  </span>
                </td>
                <td className="px-4 py-3 text-xs text-slate-500">{d.cadence}</td>
                <td className="px-4 py-3 text-xs text-slate-500">{d.lastChecked}</td>
                <td className="px-4 py-3 text-right">
                  <button
                    type="button"
                    onClick={() => onRescan?.(d.id)}
                    className="text-xs font-medium text-slate-700 hover:text-slate-900 underline focus:outline-none"
                  >
                    Rescan
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
};
