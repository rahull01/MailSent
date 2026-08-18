package com.mailsent.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * HardenedConsentRecord — Cryptographically verifiable affirmative user consent record (SEC-001 / INV-02).
 */
public class HardenedConsentRecord {

    private final String id;
    private final String organizationId;
    private final String userId;
    private final String targetActionType;
    private final String actionPayloadHash;
    private final String ipAddress;
    private final String userAgent;
    private final Instant grantedAt;

    private HardenedConsentRecord(String id, String organizationId, String userId,
                                  String targetActionType, String actionPayloadHash,
                                  String ipAddress, String userAgent, Instant grantedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.userId = Objects.requireNonNull(userId);
        this.targetActionType = Objects.requireNonNull(targetActionType);
        this.actionPayloadHash = Objects.requireNonNull(actionPayloadHash);
        this.ipAddress = Objects.requireNonNull(ipAddress);
        this.userAgent = Objects.requireNonNull(userAgent);
        this.grantedAt = Objects.requireNonNull(grantedAt);
    }

    public static HardenedConsentRecord grant(String organizationId, String userId,
                                              String actionType, String rawActionPayload,
                                              String ipAddress, String userAgent,
                                              Instant grantedAt) {
        if (organizationId == null || organizationId.isBlank() ||
                userId == null || userId.isBlank() ||
                rawActionPayload == null || rawActionPayload.isBlank()) {
            throw new IllegalArgumentException("INV-02 violation: Affirmative consent parameters cannot be empty.");
        }

        String hash = computeSha256(rawActionPayload);

        return new HardenedConsentRecord(
                UUID.randomUUID().toString(),
                organizationId,
                userId,
                actionType != null ? actionType : "GENERIC_ACTION",
                hash,
                ipAddress != null ? ipAddress : "127.0.0.1",
                userAgent != null ? userAgent : "MailSent-Client",
                grantedAt != null ? grantedAt : Instant.now()
        );
    }

    public boolean verifyPayload(String rawActionPayload) {
        if (rawActionPayload == null) return false;
        return this.actionPayloadHash.equalsIgnoreCase(computeSha256(rawActionPayload));
    }

    private static String computeSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String userId() { return userId; }
    public String targetActionType() { return targetActionType; }
    public String actionPayloadHash() { return actionPayloadHash; }
    public String ipAddress() { return ipAddress; }
    public String userAgent() { return userAgent; }
    public Instant grantedAt() { return grantedAt; }
}
