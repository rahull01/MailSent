package com.mailsent.alerts.domain;

/**
 * AlertChannel — ALERT-003 Channels (FUN-M6).
 *
 * <p>Represents the authorized delivery mechanisms for business-language alerts.
 * FUN-M6 requires support for in-app notifications, email, and third-party
 * webhooks (e.g., Slack).</p>
 */
public enum AlertChannel {
    /** Delivery within the MailSent SaaS application interface. */
    IN_APP("in-app"),
    
    /** Delivery via email to registered users. */
    EMAIL("email"),
    
    /** Delivery via configurable webhooks (e.g., Slack integration). */
    WEBHOOK("webhook");

    private final String protocol;

    AlertChannel(String protocol) {
        this.protocol = protocol;
    }

    /** The protocol identifier used for configuration and persistence. */
    public String protocol() {
        return protocol;
    }
}
