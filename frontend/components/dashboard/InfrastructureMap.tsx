"use client";

import React, { useState } from "react";

export type NodeAvailability = "OBSERVED" | "NOT_OBSERVED" | "UNAVAILABLE";
export type HealthIndication = "HEALTHY" | "AT_RISK" | "UNKNOWN";
export type InfrastructureNodeType =
  "DOMAIN" | "DNS" | "SPF" | "DKIM" | "DMARC" | "SENDERS" | "INBOX";

export interface InfrastructureMapNodeDto {
  identity: string;
  type: InfrastructureNodeType;
  availability: NodeAvailability;
  health: HealthIndication;
}

export interface InfrastructureMapRelationshipDto {
  fromIdentity: string;
  toIdentity: string;
  type: string;
}

export interface InfrastructureEvidenceRefDto {
  snapshotId: string;
  kind: string;
  observedAt: string;
}

export interface InfrastructureMapDto {
  organizationId: string;
  domainId: string;
  domainName: string;
  nodes: InfrastructureMapNodeDto[];
  relationships: InfrastructureMapRelationshipDto[];
  dnsEvidence: InfrastructureEvidenceRefDto[];
}

export interface InfrastructureMapProps {
  map: InfrastructureMapDto;
  onSelectNode?: (node: InfrastructureMapNodeDto) => void;
}

export const InfrastructureMap: React.FC<InfrastructureMapProps> = ({
  map,
  onSelectNode,
}) => {
  const [selectedNode, setSelectedNode] =
    useState<InfrastructureMapNodeDto | null>(null);

  const handleNodeClick = (node: InfrastructureMapNodeDto) => {
    setSelectedNode(node);
    if (onSelectNode) {
      onSelectNode(node);
    }
  };

  const getNodeColorClasses = (
    health: HealthIndication,
    availability: NodeAvailability,
  ) => {
    if (availability === "UNAVAILABLE") {
      return {
        bg: "bg-rose-50 border-rose-300 text-rose-800",
        badge: "bg-rose-100 text-rose-700 border-rose-200",
        dot: "bg-rose-500",
        hex: "#f43f5e",
        cardBg: "#fff1f2",
        border: "#fda4af",
      };
    }
    if (availability === "NOT_OBSERVED") {
      return {
        bg: "bg-slate-50 border-slate-200 text-slate-600",
        badge: "bg-slate-100 text-slate-600 border-slate-200",
        dot: "bg-slate-400",
        hex: "#94a3b8",
        cardBg: "#f8fafc",
        border: "#e2e8f0",
      };
    }

    switch (health) {
      case "HEALTHY":
        return {
          bg: "bg-emerald-50 border-emerald-300 text-emerald-900",
          badge: "bg-emerald-100 text-emerald-800 border-emerald-200",
          dot: "bg-emerald-500",
          hex: "#10b981",
          cardBg: "#ecfdf5",
          border: "#6ee7b7",
        };
      case "AT_RISK":
        return {
          bg: "bg-rose-50 border-rose-300 text-rose-900",
          badge: "bg-rose-100 text-rose-800 border-rose-200",
          dot: "bg-rose-500",
          hex: "#ef4444",
          cardBg: "#fef2f2",
          border: "#fca5a5",
        };
      case "UNKNOWN":
      default:
        return {
          bg: "bg-slate-50 border-slate-200 text-slate-700",
          badge: "bg-slate-100 text-slate-700 border-slate-200",
          dot: "bg-slate-400",
          hex: "#94a3b8",
          cardBg: "#f8fafc",
          border: "#e2e8f0",
        };
    }
  };

  const generateSvgString = (): string => {
    const width = 1120;
    const height = 260;
    const nodeWidth = 130;
    const nodeHeight = 90;
    const gap = 25;
    const startX = 20;
    const startY = 80;

    let svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${width} ${height}" width="${width}" height="${height}">\n`;
    svg += `  <style>\n`;
    svg += `    .title { font-family: system-ui, -apple-system, sans-serif; font-size: 16px; font-weight: bold; fill: #0f172a; }\n`;
    svg += `    .subtitle { font-family: system-ui, -apple-system, sans-serif; font-size: 11px; fill: #64748b; }\n`;
    svg += `    .node-title { font-family: system-ui, -apple-system, sans-serif; font-size: 12px; font-weight: bold; fill: #1e293b; }\n`;
    svg += `    .node-text { font-family: system-ui, -apple-system, sans-serif; font-size: 10px; fill: #475569; }\n`;
    svg += `    .node-val { font-family: monospace; font-size: 10px; font-weight: bold; fill: #0f172a; }\n`;
    svg += `  </style>\n`;
    svg += `  <rect width="${width}" height="${height}" fill="#ffffff" rx="12" stroke="#e2e8f0" stroke-width="1.5" />\n`;
    svg += `  <text x="24" y="36" class="title">Infrastructure Map: ${map.domainName}</text>\n`;
    svg += `  <text x="24" y="56" class="subtitle">Color-coded topology grounded in verified observation evidence &amp; health signals (INV-09)</text>\n`;

    map.nodes.forEach((node, i) => {
      const x = startX + i * (nodeWidth + gap);
      const y = startY;
      const colors = getNodeColorClasses(node.health, node.availability);

      // Node box
      svg += `  <g transform="translate(${x}, ${y})">\n`;
      svg += `    <rect width="${nodeWidth}" height="${nodeHeight}" rx="8" fill="${colors.cardBg}" stroke="${colors.border}" stroke-width="1.5" />\n`;
      svg += `    <circle cx="${nodeWidth - 16}" cy="18" r="5" fill="${colors.hex}" />\n`;
      svg += `    <text x="12" y="22" class="node-title">${i + 1}. ${node.type}</text>\n`;
      svg += `    <text x="12" y="48" class="node-text">Health: <tspan font-weight="bold" fill="${colors.hex}">${node.health}</tspan></text>\n`;
      svg += `    <text x="12" y="68" class="node-text">Obs: <tspan class="node-val">${node.availability}</tspan></text>\n`;
      svg += `  </g>\n`;

      // Edge arrow to next node
      if (i < map.nodes.length - 1) {
        const arrowStartX = x + nodeWidth + 4;
        const arrowEndX = x + nodeWidth + gap - 4;
        const arrowY = y + nodeHeight / 2;
        svg += `  <line x1="${arrowStartX}" y1="${arrowY}" x2="${arrowEndX}" y2="${arrowY}" stroke="#94a3b8" stroke-width="1.5" stroke-dasharray="3,3" />\n`;
        svg += `  <polygon points="${arrowEndX},${arrowY} ${arrowEndX - 4},${arrowY - 3} ${arrowEndX - 4},${arrowY + 3}" fill="#94a3b8" />\n`;
      }
    });

    svg += `</svg>`;
    return svg;
  };

  const exportSvg = () => {
    const svgString = generateSvgString();
    const blob = new Blob([svgString], { type: "image/svg+xml;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `infrastructure-map-${map.domainName}.svg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const exportPng = () => {
    const svgString = generateSvgString();
    const blob = new Blob([svgString], { type: "image/svg+xml;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement("canvas");
      canvas.width = 1120;
      canvas.height = 260;
      const ctx = canvas.getContext("2d");
      if (ctx) {
        ctx.fillStyle = "#ffffff";
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(img, 0, 0);
        const pngUrl = canvas.toDataURL("image/png");
        const link = document.createElement("a");
        link.href = pngUrl;
        link.download = `infrastructure-map-${map.domainName}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
      URL.revokeObjectURL(url);
    };
    img.src = url;
  };

  return (
    <section
      aria-labelledby="infrastructure-map-title"
      className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm space-y-6"
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2
            id="infrastructure-map-title"
            className="text-lg font-semibold text-slate-900"
          >
            Infrastructure Map: {map.domainName}
          </h2>
          <p className="text-sm text-slate-500">
            Color-coded infrastructure topology grounded in verified observation
            evidence &amp; health signals (Click any node to view details)
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {/* MAP-004 Export Controls */}
          <button
            onClick={exportSvg}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-sm"
            aria-label="Export Infrastructure Map as SVG"
          >
            ⬇️ Export SVG
          </button>
          <button
            onClick={exportPng}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-sm"
            aria-label="Export Infrastructure Map as PNG"
          >
            🖼️ Export PNG
          </button>
        </div>
      </div>

      <div className="flex items-center gap-3 text-xs">
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200">
          <span className="w-2 h-2 rounded-full bg-emerald-500" /> Healthy
          (Green)
        </span>
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-rose-50 text-rose-800 border border-rose-200">
          <span className="w-2 h-2 rounded-full bg-rose-500" /> At Risk (Red)
        </span>
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-50 text-slate-700 border border-slate-200">
          <span className="w-2 h-2 rounded-full bg-slate-400" /> Unknown (Gray)
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-7 gap-3 pt-2">
        {map.nodes.map((node, index) => {
          const color = getNodeColorClasses(node.health, node.availability);
          const isSelected = selectedNode?.identity === node.identity;
          return (
            <div
              key={node.identity}
              onClick={() => handleNodeClick(node)}
              className={`p-4 rounded-lg border flex flex-col justify-between transition-all cursor-pointer hover:shadow-sm ${color.bg} ${
                isSelected ? "ring-2 ring-slate-900 shadow-md" : ""
              }`}
              role="button"
              tabIndex={0}
              aria-label={`Node ${node.type} with status ${node.health} and availability ${node.availability}`}
            >
              <div>
                <div className="flex items-center justify-between text-xs font-semibold mb-2">
                  <span>
                    {index + 1}. {node.type}
                  </span>
                  <span className={`w-2.5 h-2.5 rounded-full ${color.dot}`} />
                </div>
                <div className="space-y-1">
                  <div className="text-xs">
                    <span className="font-medium text-slate-500">Health: </span>
                    <span
                      className={`inline-block px-1.5 py-0.5 rounded text-[10px] font-bold uppercase border ${color.badge}`}
                    >
                      {node.health}
                    </span>
                  </div>
                  <div className="text-xs">
                    <span className="font-medium text-slate-500">
                      Observation:{" "}
                    </span>
                    <span className="text-[11px] font-mono">
                      {node.availability}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* MAP-003 Node Detail Panel */}
      {selectedNode && (
        <div
          role="region"
          aria-labelledby="node-detail-title"
          className="p-5 bg-slate-50 rounded-xl border border-slate-200 mt-4 animate-in fade-in duration-200"
        >
          <div className="flex items-center justify-between border-b border-slate-200 pb-3 mb-4">
            <div className="flex items-center gap-3">
              <span className="px-2.5 py-1 rounded-md bg-slate-900 text-white font-mono text-xs font-semibold">
                {selectedNode.type}
              </span>
              <h3
                id="node-detail-title"
                className="text-base font-semibold text-slate-900"
              >
                Node Identity:{" "}
                <code className="text-xs text-slate-600 bg-white px-2 py-0.5 rounded border border-slate-200">
                  {selectedNode.identity}
                </code>
              </h3>
            </div>
            <button
              onClick={() => setSelectedNode(null)}
              className="text-slate-400 hover:text-slate-700 text-sm font-medium px-2 py-1 rounded hover:bg-slate-200"
              aria-label="Close node detail panel"
            >
              ✕ Close
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div className="p-3 bg-white rounded-lg border border-slate-200">
              <span className="text-xs font-medium text-slate-500 block mb-1">
                Observation Status
              </span>
              <span className="text-sm font-mono font-semibold text-slate-800">
                {selectedNode.availability}
              </span>
            </div>
            <div className="p-3 bg-white rounded-lg border border-slate-200">
              <span className="text-xs font-medium text-slate-500 block mb-1">
                Health Indication (MAP-002 / HEALTH-001)
              </span>
              <span
                className={`inline-block px-2 py-0.5 rounded text-xs font-bold uppercase border ${
                  selectedNode.health === "HEALTHY"
                    ? "bg-emerald-50 text-emerald-800 border-emerald-200"
                    : selectedNode.health === "AT_RISK"
                      ? "bg-rose-50 text-rose-800 border-rose-200"
                      : "bg-slate-100 text-slate-700 border-slate-200"
                }`}
              >
                {selectedNode.health}
              </span>
            </div>
            <div className="p-3 bg-white rounded-lg border border-slate-200">
              <span className="text-xs font-medium text-slate-500 block mb-1">
                Domain Scope (INV-09)
              </span>
              <span className="text-sm font-semibold text-slate-800">
                {map.domainName}
              </span>
            </div>
          </div>

          {/* Node Specific Factual Evidence */}
          {selectedNode.availability === "NOT_OBSERVED" ? (
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800">
              ℹ️ <strong>Observation Not Configured:</strong> No raw observation
              snapshot is recorded for {selectedNode.type}. Details are not
              fabricated.
            </div>
          ) : selectedNode.availability === "UNAVAILABLE" ? (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-xs text-rose-800">
              ⚠️ <strong>Observation Unavailable:</strong> Live inspection
              failed or DNS record not found for {selectedNode.type}.
            </div>
          ) : (
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-600">
                Attached Factual Evidence References
              </h4>
              {map.dnsEvidence && map.dnsEvidence.length > 0 ? (
                <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
                  <table className="min-w-full divide-y divide-slate-200 text-xs">
                    <thead className="bg-slate-50 text-slate-600 font-semibold">
                      <tr>
                        <th scope="col" className="px-3 py-2 text-left">
                          Evidence Kind
                        </th>
                        <th scope="col" className="px-3 py-2 text-left">
                          Snapshot ID
                        </th>
                        <th scope="col" className="px-3 py-2 text-left">
                          Observed Timestamp
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200 font-mono text-[11px] text-slate-700">
                      {map.dnsEvidence.map((ref) => (
                        <tr key={ref.snapshotId} className="hover:bg-slate-50">
                          <td className="px-3 py-2 font-sans font-medium text-slate-800">
                            {ref.kind}
                          </td>
                          <td className="px-3 py-2">{ref.snapshotId}</td>
                          <td className="px-3 py-2 text-slate-500">
                            {ref.observedAt}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="text-xs text-slate-500 italic">
                  No secondary evidence attachments recorded for this node.
                </p>
              )}
            </div>
          )}
        </div>
      )}
    </section>
  );
};
