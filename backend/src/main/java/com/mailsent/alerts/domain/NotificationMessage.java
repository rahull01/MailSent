package com.mailsent.alerts.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * NotificationMessage — Output message dispatched to external user channels (NOTIF-001).
 */
public class NotificationMessage {

    public enum DispatchStatus {
        PENDING,
        SENT,
        FAILED
    }

    private final String id;
    private final String recipientUserId;
    private final String recipientAddress;
    private final AlertChannel channel;
    private final String subject;
    private final String body;
    private DispatchStatus status;
    private final Instant createdAt;
    private Instant sentAt;

    private NotificationMessage(String id, String recipientUserId, String recipientAddress,
                                AlertChannel channel, String subject, String body,
                                DispatchStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.recipientUserId = Objects.requireNonNull(recipientUserId);
        this.recipientAddress = Objects.requireNonNull(recipientAddress);
        this.channel = Objects.requireNonNull(channel);
        this.subject = Objects.requireNonNull(subject);
        this.body = Objects.requireNonNull(body);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static NotificationMessage create(String recipientUserId, String recipientAddress,
                                             AlertChannel channel, String subject, String body,
                                             Instant createdAt) {
        if (recipientUserId.isBlank() || recipientAddress.isBlank() || subject.isBlank() || body.isBlank()) {
            throw new IllegalArgumentException("Notification fields cannot be blank.");
        }
        return new NotificationMessage(
                UUID.randomUUID().toString(),
                recipientUserId,
                recipientAddress,
                channel,
                subject,
                body,
                DispatchStatus.PENDING,
                createdAt != null ? createdAt : Instant.now()
        );
    }

    public void markSent(Instant sentAt) {
        this.status = DispatchStatus.SENT;
        this.sentAt = Objects.requireNonNull(sentAt);
    }

    public void markFailed() {
        this.status = DispatchStatus.FAILED;
    }

    public String id() { return id; }
    public String recipientUserId() { return recipientUserId; }
    public String recipientAddress() { return recipientAddress; }
    public AlertChannel channel() { return channel; }
    public String subject() { return subject; }
    public String body() { return body; }
    public DispatchStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant sentAt() { return sentAt; }
}
