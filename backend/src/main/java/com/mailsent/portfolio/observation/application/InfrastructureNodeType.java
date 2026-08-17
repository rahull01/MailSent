package com.mailsent.portfolio.observation.application;

/**
 * Frozen WF-5 topology node types for MAP-001. Reputation, DNS Health, and
 * Blacklist are not node types.
 */
public enum InfrastructureNodeType {
    DOMAIN,
    DNS,
    SPF,
    DKIM,
    DMARC,
    SENDERS,
    INBOX
}
