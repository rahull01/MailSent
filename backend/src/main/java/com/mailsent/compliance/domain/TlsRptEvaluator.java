package com.mailsent.compliance.domain;

import java.util.List;
import java.util.Locale;

/**
 * Pure DNS-level evaluator for TLS-RPT (RFC 8460) records.
 */
public final class TlsRptEvaluator {

    private TlsRptEvaluator() {}

    public static ProviderRequirement evaluate(List<String> txtRecords, String evidenceRef) {
        if (txtRecords == null) {
            return ProviderRequirement.of(
                "TLS-RPT Readiness",
                "SMTP TLS Reporting record published at _smtp._tls",
                "Receives aggregated reports on inbound TLS connection failures",
                RequirementStatus.UNKNOWN,
                null
            );
        }

        String tlsRptRecord = null;
        for (String txt : txtRecords) {
            if (txt != null && txt.trim().toLowerCase(Locale.ROOT).startsWith("v=tlsrptv1")) {
                tlsRptRecord = txt.trim();
                break;
            }
        }

        if (tlsRptRecord == null) {
            return ProviderRequirement.of(
                "TLS-RPT Readiness",
                "SMTP TLS Reporting record published at _smtp._tls",
                "Receives aggregated reports on inbound TLS connection failures",
                RequirementStatus.NOT_SATISFIED,
                evidenceRef
            );
        }

        // Bounded tag parsing
        String[] tags = tlsRptRecord.split(";");
        boolean hasValidVersion = false;
        boolean hasValidRua = false;

        for (String rawTag : tags) {
            String tag = rawTag.trim();
            int eq = tag.indexOf('=');
            if (eq > 0) {
                String key = tag.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String val = tag.substring(eq + 1).trim();
                if (key.equals("v") && val.equalsIgnoreCase("TLSRPTv1")) {
                    hasValidVersion = true;
                } else if (key.equals("rua") && !val.isBlank()) {
                    String lowerVal = val.toLowerCase(Locale.ROOT);
                    if (lowerVal.startsWith("mailto:") || lowerVal.startsWith("https://")) {
                        hasValidRua = true;
                    }
                }
            }
        }

        RequirementStatus status = (hasValidVersion && hasValidRua)
            ? RequirementStatus.SATISFIED
            : RequirementStatus.WARNING;

        return ProviderRequirement.of(
            "TLS-RPT Readiness",
            "SMTP TLS Reporting record published at _smtp._tls",
            "Receives aggregated reports on inbound TLS connection failures",
            status,
            evidenceRef
        );
    }
}
