"use client";

import React, { useState } from "react";

export interface WhiteLabelConfigState {
  agencyName: string;
  logoUrl: string;
  accentColor: string;
  customDisclaimer: string;
  customFooter: string;
}

export interface WhiteLabelConfigModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentConfig: WhiteLabelConfigState;
  onSave: (config: WhiteLabelConfigState) => void;
  onResetToDefault: () => void;
  activeOrgName?: string;
}

const HEX_PATTERN = /^#([A-Fa-f0-9]{6})$/;

export function validateWhiteLabelInput(config: WhiteLabelConfigState): {
  isValid: boolean;
  errors: Record<string, string>;
} {
  const errors: Record<string, string> = {};

  if (!config.agencyName || config.agencyName.trim().length === 0) {
    errors.agencyName = "Agency name is required and cannot be blank.";
  }

  if (config.accentColor) {
    if (!HEX_PATTERN.test(config.accentColor.trim())) {
      errors.accentColor =
        "Accent color must match #RRGGBB format (e.g. #0F172A).";
    }
  }

  if (config.logoUrl && config.logoUrl.trim().length > 0) {
    const trimmed = config.logoUrl.trim();
    const lower = trimmed.toLowerCase();

    if (
      lower.startsWith("javascript:") ||
      lower.startsWith("data:") ||
      lower.startsWith("vbscript:") ||
      lower.includes("<") ||
      lower.includes(">") ||
      lower.includes('"')
    ) {
      errors.logoUrl = "Unsafe logo URL or script payload detected.";
    } else if (!trimmed.startsWith("https://") && !trimmed.startsWith("/")) {
      errors.logoUrl =
        "Logo URL must begin with https:// or be a relative path starting with /.";
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

export const WhiteLabelConfigModal: React.FC<WhiteLabelConfigModalProps> = ({
  isOpen,
  onClose,
  currentConfig,
  onSave,
  onResetToDefault,
  activeOrgName,
}) => {
  const [formData, setFormData] =
    useState<WhiteLabelConfigState>(currentConfig);
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

  // Sync state if modal reopens with updated props
  React.useEffect(() => {
    setFormData(currentConfig);
    setValidationErrors({});
  }, [currentConfig, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const { isValid, errors } = validateWhiteLabelInput(formData);
    if (!isValid) {
      setValidationErrors(errors);
      return;
    }
    setValidationErrors({});
    onSave({
      agencyName: formData.agencyName.trim(),
      logoUrl: formData.logoUrl.trim(),
      accentColor: formData.accentColor.trim().toUpperCase() || "#0F172A",
      customDisclaimer: formData.customDisclaimer.trim(),
      customFooter: formData.customFooter.trim(),
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
      <div className="bg-white rounded-xl shadow-xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-150">
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900">
              White-Label Branding Settings
            </h3>
            <p className="text-xs text-slate-500">
              Active Context:{" "}
              <span className="font-medium text-slate-700">
                {activeOrgName || "Current Agency"}
              </span>
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 text-lg leading-none p-1"
          >
            ✕
          </button>
        </div>

        {/* Persistence boundary alert */}
        <div className="bg-amber-50 border-y border-amber-200/60 px-6 py-2.5 flex items-start gap-2">
          <span className="text-amber-600 text-sm">ℹ️</span>
          <p className="text-xs text-amber-800 leading-relaxed">
            <span className="font-semibold">
              Preview / Active for this session:
            </span>{" "}
            Agency white-labeling is applied in-memory to client drilldowns and
            PDF exports for this active agency session. No database schema is
            altered.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Agency Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              value={formData.agencyName}
              onChange={(e) =>
                setFormData({ ...formData, agencyName: e.target.value })
              }
              placeholder="e.g. Apex Growth Agency"
              className={`w-full text-sm px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                validationErrors.agencyName
                  ? "border-rose-400 focus:ring-rose-200"
                  : "border-slate-300 focus:ring-slate-900"
              }`}
            />
            {validationErrors.agencyName && (
              <p className="text-xs text-rose-600 mt-1">
                {validationErrors.agencyName}
              </p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Accent Color (Hex)
              </label>
              <div className="flex items-center gap-2">
                <input
                  type="color"
                  value={
                    HEX_PATTERN.test(formData.accentColor)
                      ? formData.accentColor
                      : "#0F172A"
                  }
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      accentColor: e.target.value.toUpperCase(),
                    })
                  }
                  className="h-9 w-9 rounded border border-slate-300 cursor-pointer p-0.5"
                />
                <input
                  type="text"
                  value={formData.accentColor}
                  onChange={(e) =>
                    setFormData({ ...formData, accentColor: e.target.value })
                  }
                  placeholder="#0F172A"
                  className={`w-full text-sm font-mono px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                    validationErrors.accentColor
                      ? "border-rose-400 focus:ring-rose-200"
                      : "border-slate-300 focus:ring-slate-900"
                  }`}
                />
              </div>
              {validationErrors.accentColor && (
                <p className="text-xs text-rose-600 mt-1">
                  {validationErrors.accentColor}
                </p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Logo URL (HTTPS / Path)
              </label>
              <input
                type="text"
                value={formData.logoUrl}
                onChange={(e) =>
                  setFormData({ ...formData, logoUrl: e.target.value })
                }
                placeholder="https://... or /logo.png"
                className={`w-full text-sm px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                  validationErrors.logoUrl
                    ? "border-rose-400 focus:ring-rose-200"
                    : "border-slate-300 focus:ring-slate-900"
                }`}
              />
              {validationErrors.logoUrl && (
                <p className="text-xs text-rose-600 mt-1">
                  {validationErrors.logoUrl}
                </p>
              )}
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Custom Disclaimer (Optional)
            </label>
            <input
              type="text"
              value={formData.customDisclaimer}
              onChange={(e) =>
                setFormData({ ...formData, customDisclaimer: e.target.value })
              }
              placeholder="e.g. Confidential deliverability audit prepared for client..."
              className="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Custom Footer (Optional)
            </label>
            <input
              type="text"
              value={formData.customFooter}
              onChange={(e) =>
                setFormData({ ...formData, customFooter: e.target.value })
              }
              placeholder="e.g. Generated by Apex Growth Agency Security Operations"
              className="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>

          <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
            <button
              type="button"
              onClick={() => {
                onResetToDefault();
                onClose();
              }}
              className="text-xs font-semibold text-slate-600 hover:text-slate-900 underline"
            >
              Reset to Neutral MailSent
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className="text-xs font-semibold px-4 py-2 border border-slate-300 rounded-lg hover:bg-slate-50 text-slate-700"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="text-xs font-bold px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 shadow-sm"
              >
                Apply for Session
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
