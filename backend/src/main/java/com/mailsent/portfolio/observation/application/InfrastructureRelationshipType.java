package com.mailsent.portfolio.observation.application;

/**
 * Typed edges of the frozen WF-5 chain (MAP-001):
 * Domain → DNS → SPF → DKIM → DMARC → Senders → Inbox.
 */
public enum InfrastructureRelationshipType {
    DOMAIN_TO_DNS,
    DNS_TO_SPF,
    SPF_TO_DKIM,
    DKIM_TO_DMARC,
    DMARC_TO_SENDERS,
    SENDERS_TO_INBOX
}
