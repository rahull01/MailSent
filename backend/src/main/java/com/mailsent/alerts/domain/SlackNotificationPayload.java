package com.mailsent.alerts.domain;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * SlackNotificationPayload — Formats MailSent alerts into structured Slack blocks (INT-001).
 */
public class SlackNotificationPayload {

    private final String id;
    private final String webhookUrl;
    private final String channelName;
    private final String jsonBody;

    private SlackNotificationPayload(String id, String webhookUrl, String channelName, String jsonBody) {
        this.id = Objects.requireNonNull(id);
        this.webhookUrl = Objects.requireNonNull(webhookUrl);
        this.channelName = channelName;
        this.jsonBody = Objects.requireNonNull(jsonBody);
    }

    public static SlackNotificationPayload fromAlert(String webhookUrl, String channelName, Alert alert) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("Webhook URL must not be blank.");
        }
        Objects.requireNonNull(alert);

        // Simple escaped JSON builder for Slack webhook compatibility
        String escapedWhat = escapeJson(alert.whatHappened());
        String escapedWhy = escapeJson(alert.whyItMatters());
        String escapedAction = alert.actionRequired() != null ? escapeJson(alert.actionRequired()) : "None required";

        String json = String.format(
                "{\"text\":\"[%s] MailSent Alert: %s\",\"blocks\":[{\"type\":\"header\",\"text\":{\"type\":\"plain_text\",\"text\":\"🚨 MailSent Deliverability Alert\"}},{\"type\":\"section\",\"fields\":[{\"type\":\"mrkdwn\",\"text\":\"*Severity:* %s\"},{\"type\":\"mrkdwn\",\"text\":\"*What happened:*\\n%s\"},{\"type\":\"mrkdwn\",\"text\":\"*Impact:*\\n%s\"},{\"type\":\"mrkdwn\",\"text\":\"*Action:*\\n%s\"}]}]}",
                alert.severity(),
                escapedWhat,
                alert.severity(),
                escapedWhat,
                escapedWhy,
                escapedAction
        );

        return new SlackNotificationPayload(UUID.randomUUID().toString(), webhookUrl, channelName, json);
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public String id() { return id; }
    public String webhookUrl() { return webhookUrl; }
    public String channelName() { return channelName; }
    public String jsonBody() { return jsonBody; }
}
