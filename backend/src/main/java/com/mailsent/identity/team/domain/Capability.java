package com.mailsent.identity.team.domain;

/**
 * Capability — an explicit, strongly typed authorization permission token (TEAM-002, Identity context).
 *
 * <p>Every capability is explicitly mapped to bounded context operations and justified by
 * the frozen product requirements and architecture (FUN-D5, NFR-S1, 09_SECURITY_ARCHITECTURE.md).
 * No implicit inheritance exists; authorization is strictly default-deny.</p>
 */
public enum Capability {

    /** Read Health Signal, Health Trend, and raw evidence snapshots (Delivery Observation). */
    HEALTH_VIEW,

    /** Read Issues, Root Causes, and plain-language explanations (Diagnosis & Explanation). */
    DIAGNOSIS_VIEW,

    /** View and export executive summaries and evidence proofs (Reporting & Evidence). */
    REPORT_VIEW,

    /** View mailbox provider readiness checklists (Readiness & Compliance). */
    READINESS_VIEW,

    /** View topology and node detail panels (Infrastructure Map). */
    MAP_VIEW,

    /** Request immediate manual domain re-scan / re-observation (Delivery Observation). */
    DOMAIN_OBSERVE_TRIGGER,

    /** Trigger fix verification checks (Guidance & Resolution). */
    RESOLUTION_VERIFY,

    /** Add, pause, or remove domains in the portfolio registry (Portfolio & Client). */
    DOMAIN_MANAGE,

    /** Grant or revoke infrastructure observation consent (INV-02, Identity & Access). */
    CONSENT_GRANT,

    /** Issue email invitations to join the organization (Identity / Team). */
    TEAM_INVITE,

    /** Update team member roles or remove members (Identity / Team). */
    TEAM_MANAGE_ROLES,

    /** Manage organization profile, notification channels, and webhooks (Settings / Admin). */
    ORG_SETTINGS_MANAGE,

    /** Manage billing, subscription tier, and payment methods (Billing). */
    BILLING_MANAGE
}
