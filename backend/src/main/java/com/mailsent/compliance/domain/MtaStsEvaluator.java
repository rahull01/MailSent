package com.mailsent.compliance.domain;

import java.util.List;
import java.util.Locale;

/**
 * Pure DNS-level evaluator for MTA-STS (RFC 8461) records.
 */
public final class MtaStsEvaluator {

    private MtaStsEvaluator() {}

    public static ProviderRequirement evaluate(List<String> txtRecords, String evidenceRef) {
        if (txtRecords == null) {
            return ProviderRequirement.of(
                "MTA-STS Readiness",
                "MTA-STS policy record published at _mta-sts",
                "Protects inbound email from man-in-the-middle downgrade attacks",
                RequirementStatus.UNKNOWN,
                null
            );
        }

        String mtaStsRecord = null;
        for (String txt : txtRecords) {
            if (txt != null && txt.trim().toLowerCase(Locale.ROOT).startsWith("v=stsv1")) {
                mtaStsRecord = txt.trim();
                break;
            }
        }

        if (mtaStsRecord == null) {
            return ProviderRequirement.of(
                "MTA-STS Readiness",
                "MTA-STS policy record published at _mta-sts",
                "Protects inbound email from man-in-the-middle downgrade attacks",
                RequirementStatus.NOT_SATISFIED,
                evidenceRef
            );
        }

        // Bounded tag parsing
        String[] tags = mtaStsRecord.split(";");
        boolean hasValidVersion = false;
        boolean hasValidId = false;

        for (String rawTag : tags) {
            String tag = rawTag.trim();
            int eq = tag.indexOf('=');
            if (eq > 0) {
                String key = tag.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String val = tag.substring(eq + 1).trim();
                if (key.equals("v") && val.equalsIgnoreCase("STSv1")) {
                    hasValidVersion = true;
                } else if (key.equals("id") && !val.isBlank()) {
                    hasValidId = true;
                }
            }
        }

        RequirementStatus status = (hasValidVersion && hasValidId)
            ? RequirementStatus.SATISFIED
            : RequirementStatus.WARNING;

        return ProviderRequirement.of(
            "MTA-STS Readiness",
            "MTA-STS policy record published at _mta-sts",
            "Protects inbound email from man-in-the-middle downgrade attacks",
            status,
            evidenceRef
        );
    }
}
