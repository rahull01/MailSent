package com.mailsent.resolution.application;

import java.util.List;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.resolution.domain.ResolutionAction;

public class ResolutionService {

    public List<ResolutionAction> generateActions(HealthSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("Health signal is required to generate resolution actions");
        }

        if (signal.indication() == HealthIndication.AT_RISK) {
            return List.of(
                ResolutionAction.create(
                    "Identify failing sender",
                    "Review recent DMARC aggregate reports to pinpoint the source of unaligned emails.",
                    List.of("Open the DMARC reports dashboard for " + signal.domainName(), "Filter by disposition=quarantine or reject"),
                    1
                ),
                ResolutionAction.create(
                    "Fix DNS records",
                    "Ensure SPF and DKIM are valid and correctly aligned.",
                    List.of("Check your DNS provider for TXT records", "Update the SPF include statement or DKIM keys as required"),
                    2
                )
            );
        } else if (signal.indication() == HealthIndication.UNKNOWN) {
            return List.of(
                ResolutionAction.create(
                    "Publish DMARC record",
                    "You need a DMARC record to receive reports.",
                    List.of("Log in to your DNS provider", "Add a TXT record for _dmarc with v=DMARC1; p=none; rua=mailto:reports@mailsent.com"),
                    1
                )
            );
        }

        return List.of();
    }
}
