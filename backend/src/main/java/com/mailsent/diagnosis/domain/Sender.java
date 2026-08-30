package com.mailsent.diagnosis.domain;

/**
 * Sender — A recognized party that sends on behalf of a domain.
 * 
 * <p>Sender detection is DIAG-002 (Alignment Intelligence). It identifies the 
 * underlying email service provider (e.g. Google Workspace, Microsoft 365, 
 * Salesforce) from observation evidence facts.</p>
 */
public record Sender(String providerId, String providerName) {
    public Sender {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("providerId cannot be null or blank");
        }
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("providerName cannot be null or blank");
        }
    }
}
