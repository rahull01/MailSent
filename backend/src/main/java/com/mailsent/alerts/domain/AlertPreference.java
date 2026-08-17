package com.mailsent.alerts.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * AlertPreference — ALERT-004 Digest + control (FUN-M7).
 *
 * <p>Represents a user's configuration for receiving alerts. FUN-M7 strictly requires
 * that the user controls what they are alerted to (filtered by severity) and how
 * (the delivery mode and channel), explicitly requiring a digest option and the
 * ability to mute alerts.</p>
 */
public class AlertPreference {

    public enum DeliveryMode {
        /** Deliver the alert immediately upon generation. */
        IMMEDIATE,
        
        /** Queue the alert for a scheduled batch digest. */
        DIGEST,
        
        /** Suppress the alert entirely. */
        MUTED
    }

    private final String id;
    private final String userId;
    private final AlertSeverity minimumSeverity;
    private final DeliveryMode deliveryMode;
    private final AlertChannel channel;

    private AlertPreference(String id, String userId, AlertSeverity minimumSeverity, 
                            DeliveryMode deliveryMode, AlertChannel channel) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.minimumSeverity = Objects.requireNonNull(minimumSeverity);
        this.deliveryMode = Objects.requireNonNull(deliveryMode);
        
        // Channel can be null if MUTED, but otherwise is required
        if (deliveryMode != DeliveryMode.MUTED && channel == null) {
            throw new IllegalArgumentException("A delivery channel is required unless the preference is MUTED.");
        }
        this.channel = channel;
    }

    /**
     * Configures a new alert preference for a user.
     *
     * @param userId          the user's identity
     * @param minimumSeverity the minimum severity required to trigger this preference
     * @param deliveryMode    immediate, digest, or muted (FUN-M7)
     * @param channel         the delivery channel (can be null if muted)
     * @return a new AlertPreference instance
     */
    public static AlertPreference configure(String userId, AlertSeverity minimumSeverity, 
                                            DeliveryMode deliveryMode, AlertChannel channel) {
        return new AlertPreference(UUID.randomUUID().toString(), userId, minimumSeverity, deliveryMode, channel);
    }

    /** Mutes all alerts by defaulting to MUTED mode with no channel. */
    public static AlertPreference muteAll(String userId) {
        return new AlertPreference(UUID.randomUUID().toString(), userId, AlertSeverity.INFO, DeliveryMode.MUTED, null);
    }

    public String id() { return id; }
    public String userId() { return userId; }
    public AlertSeverity minimumSeverity() { return minimumSeverity; }
    public DeliveryMode deliveryMode() { return deliveryMode; }
    public AlertChannel channel() { return channel; }
}
