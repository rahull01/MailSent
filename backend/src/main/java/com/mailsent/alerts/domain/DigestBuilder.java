package com.mailsent.alerts.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DigestBuilder — Compiles multiple alerts into a single scheduled digest notification (NOTIF-003).
 */
public final class DigestBuilder {

    private DigestBuilder() {}

    public static NotificationMessage buildDigest(String recipientUserId, String recipientAddress,
                                                  AlertChannel channel, List<Alert> alerts,
                                                  Instant generatedAt) {
        Objects.requireNonNull(recipientUserId);
        Objects.requireNonNull(recipientAddress);
        Objects.requireNonNull(channel);
        if (alerts == null || alerts.isEmpty()) {
            throw new IllegalArgumentException("Cannot build a digest with zero alerts.");
        }

        long criticalCount = alerts.stream().filter(a -> a.severity() == AlertSeverity.CRITICAL).count();
        long warningCount = alerts.stream().filter(a -> a.severity() == AlertSeverity.WARNING).count();
        long infoCount = alerts.stream().filter(a -> a.severity() == AlertSeverity.INFO).count();

        String subject = String.format("MailSent Deliverability Digest: %d Critical, %d Warnings", criticalCount, warningCount);

        StringBuilder body = new StringBuilder();
        body.append("Your scheduled MailSent infrastructure digest is ready.\n\n");
        body.append(String.format("Summary: %d Critical | %d Warnings | %d Informational\n\n", criticalCount, warningCount, infoCount));
        body.append("Key Findings:\n");

        for (Alert a : alerts) {
            body.append(String.format("- [%s] %s (Impact: %s)\n", a.severity(), a.whatHappened(), a.whyItMatters()));
        }

        body.append("\nLog in to your MailSent control hub to view recommendations and apply fixes.");

        return NotificationMessage.create(
                recipientUserId,
                recipientAddress,
                channel,
                subject,
                body.toString(),
                generatedAt != null ? generatedAt : Instant.now()
        );
    }
}
