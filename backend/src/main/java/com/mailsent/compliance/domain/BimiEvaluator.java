package com.mailsent.compliance.domain;

import java.util.List;
import java.util.Locale;

/**
 * Pure DNS-level evaluator for BIMI (Brand Indicators for Message Identification) records.
 */
public final class BimiEvaluator {

    private BimiEvaluator() {}

    public static ProviderRequirement evaluate(List<String> txtRecords, String evidenceRef) {
        if (txtRecords == null) {
            return ProviderRequirement.of(
                "BIMI Readiness",
                "BIMI record published at default._bimi",
                "Displays verified brand logo in supporting mailbox provider inboxes",
                RequirementStatus.UNKNOWN,
                null
            );
        }

        String bimiRecord = null;
        for (String txt : txtRecords) {
            if (txt != null && txt.trim().toLowerCase(Locale.ROOT).startsWith("v=bimi1")) {
                bimiRecord = txt.trim();
                break;
            }
        }

        if (bimiRecord == null) {
            return ProviderRequirement.of(
                "BIMI Readiness",
                "BIMI record published at default._bimi",
                "Displays verified brand logo in supporting mailbox provider inboxes",
                RequirementStatus.NOT_SATISFIED,
                evidenceRef
            );
        }

        // Bounded tag parsing
        String[] tags = bimiRecord.split(";");
        boolean hasValidVersion = false;
        boolean hasValidLogo = false;

        for (String rawTag : tags) {
            String tag = rawTag.trim();
            int eq = tag.indexOf('=');
            if (eq > 0) {
                String key = tag.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String val = tag.substring(eq + 1).trim();
                if (key.equals("v") && val.equalsIgnoreCase("BIMI1")) {
                    hasValidVersion = true;
                } else if (key.equals("l") && !val.isBlank()) {
                    String lowerVal = val.toLowerCase(Locale.ROOT);
                    if (lowerVal.startsWith("https://")) {
                        hasValidLogo = true;
                    }
                }
            }
        }

        RequirementStatus status = (hasValidVersion && hasValidLogo)
            ? RequirementStatus.SATISFIED
            : RequirementStatus.WARNING;

        return ProviderRequirement.of(
            "BIMI Readiness",
            "BIMI record published at default._bimi",
            "Displays verified brand logo in supporting mailbox provider inboxes",
            status,
            evidenceRef
        );
    }
}
