"use client";

import React, { useState } from "react";

export interface ClientSeat {
  id: string;
  email: string;
  role: "VIEWER" | "CLIENT_ADMIN";
  assignedDomains: string[];
  invitedAt: string;
  status: "ACTIVE" | "PENDING";
}

export interface ClientSeatManagerProps {
  clientId: string;
  clientName: string;
  seats: ClientSeat[];
  onInviteSeat?: (email: string, role: "VIEWER" | "CLIENT_ADMIN") => void;
  onRevokeSeat?: (seatId: string) => void;
}

export const ClientSeatManager: React.FC<ClientSeatManagerProps> = ({
  clientName,
  seats,
  onInviteSeat,
  onRevokeSeat,
}) => {
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState<"VIEWER" | "CLIENT_ADMIN">(
    "VIEWER",
  );

  const handleInvite = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteEmail) return;
    onInviteSeat?.(inviteEmail, inviteRole);
    setInviteEmail("");
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-6 space-y-6">
      <div className="flex items-center justify-between border-b pb-4 border-slate-100">
        <div>
          <h2 className="text-lg font-bold text-slate-900">
            Viewer & Stakeholder Seats
          </h2>
          <p className="text-xs text-slate-500">
            Grant read-only or administrative portal access to contacts at{" "}
            {clientName}
          </p>
        </div>
      </div>

      <form onSubmit={handleInvite} className="flex gap-3 items-center">
        <input
          type="email"
          placeholder="stakeholder@client.com"
          value={inviteEmail}
          onChange={(e) => setInviteEmail(e.target.value)}
          required
          className="flex-1 text-sm border border-slate-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-slate-900 focus:outline-none"
        />
        <select
          value={inviteRole}
          onChange={(e) =>
            setInviteRole(e.target.value as "VIEWER" | "CLIENT_ADMIN")
          }
          className="text-sm border border-slate-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-slate-900 focus:outline-none bg-white"
        >
          <option value="VIEWER">Read-Only Viewer</option>
          <option value="CLIENT_ADMIN">Client Admin</option>
        </select>
        <button
          type="submit"
          className="text-xs font-bold px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 transition-colors"
        >
          Send Invite
        </button>
      </form>

      <div className="divide-y divide-slate-100 border border-slate-200 rounded-lg overflow-hidden">
        {seats.map((seat) => (
          <div
            key={seat.id}
            className="p-4 flex items-center justify-between hover:bg-slate-50"
          >
            <div className="space-y-0.5">
              <div className="flex items-center gap-2">
                <span className="font-semibold text-sm text-slate-900">
                  {seat.email}
                </span>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                  {seat.role}
                </span>
                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded ${seat.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}`}
                >
                  {seat.status}
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Invited on {seat.invitedAt} • Scoped to{" "}
                {seat.assignedDomains.length} domains
              </p>
            </div>
            {onRevokeSeat && (
              <button
                type="button"
                onClick={() => onRevokeSeat(seat.id)}
                className="text-xs text-rose-600 hover:text-rose-800 font-medium"
              >
                Revoke
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
