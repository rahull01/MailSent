package com.mailsent.alerts.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * NotificationInventory — Historical audit trail of all sent notifications (NOTIF-002).
 */
public class NotificationInventory {

    private final String id;
    private final String organizationId;
    private final List<NotificationMessage> notifications;
    private final Instant generatedAt;

    public NotificationInventory(String id, String organizationId,
                                 List<NotificationMessage> notifications,
                                 Instant generatedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.notifications = Collections.unmodifiableList(Objects.requireNonNull(notifications));
        this.generatedAt = Objects.requireNonNull(generatedAt);
    }

    public static NotificationInventory of(String organizationId, List<NotificationMessage> notifications, Instant generatedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        return new NotificationInventory(
                UUID.randomUUID().toString(),
                organizationId,
                notifications != null ? notifications : Collections.emptyList(),
                generatedAt != null ? generatedAt : Instant.now()
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public List<NotificationMessage> notifications() { return notifications; }
    public Instant generatedAt() { return generatedAt; }
}
