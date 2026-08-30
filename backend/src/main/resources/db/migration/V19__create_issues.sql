-- =============================================================================
-- MailSent — Flyway migration V19: issues
-- DIAG-004 (Issue Entity Lifecycle) production schema.
-- =============================================================================

CREATE TABLE IF NOT EXISTS issues (
    id                                      VARCHAR(36)  NOT NULL,
    organization_id                         VARCHAR(36)  NOT NULL,
    domain_id                               VARCHAR(36)  NOT NULL,
    domain_name                             VARCHAR(253) NOT NULL,
    root_cause                              VARCHAR(64)  NOT NULL,
    provider_id                             VARCHAR(64),
    provider_name                           VARCHAR(128),
    status                                  VARCHAR(32)  NOT NULL,
    origin_health_signal_id                 VARCHAR(36)  NOT NULL,
    origin_dmarc_aggregate_evidence_ids     TEXT         NOT NULL,
    latest_health_signal_id                 VARCHAR(36)  NOT NULL,
    latest_dmarc_aggregate_evidence_ids     TEXT         NOT NULL,
    discovered_at                           TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at                             TIMESTAMP WITH TIME ZONE,
    closed_at                               TIMESTAMP WITH TIME ZONE,
    updated_at                              TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_issues PRIMARY KEY (id),
    CONSTRAINT fk_issues_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_issues_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_issues_organization_id ON issues (organization_id);
CREATE INDEX idx_issues_domain_id ON issues (domain_id);
CREATE INDEX idx_issues_status ON issues (organization_id, status);

CREATE UNIQUE INDEX uk_issues_active_fingerprint 
ON issues (organization_id, domain_id, root_cause, COALESCE(provider_id, '')) 
WHERE status IN ('DISCOVERED', 'DIAGNOSED', 'RESOLVED');
