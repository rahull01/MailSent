-- =============================================================================
-- MailSent — Flyway migration V15: dmarc_aggregate_evidence
-- INT-005 (DMARC Aggregate Evidence Store) production schema. Applied
-- automatically by Flyway at boot, BEFORE Spring Data JPA validates
-- (ddl-auto: validate in the 'it' profile).
--
-- DMARC Aggregate Evidence is an append-only aggregated RUA snapshot for a
-- registered Domain (INV-12). Tenant binding is the Receipt's organization_id
-- and domain_id (INV-09), never XML policy_published/domain. One report may
-- yield multiple rows (one per source_ip); there is therefore no UNIQUE on
-- (domain_id, report_id) alone, and no UNIQUE on domain_id. Duplicate
-- report_id + Domain idempotency is application-level (skip the whole report
-- if any snapshot already exists). Lineage: receipt_id FK plus report_id TEXT.
-- utc_day is the UTC calendar date of date_range.begin. dkim_domains is TEXT
-- JSON array (OE style). No JSONB. No Alignment / Observation Evidence /
-- Health columns. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS dmarc_aggregate_evidence (
    id                   VARCHAR(36)  NOT NULL,
    organization_id      VARCHAR(36)  NOT NULL,
    domain_id            VARCHAR(36)  NOT NULL,
    domain_name          VARCHAR(253) NOT NULL,
    receipt_id           VARCHAR(36)  NOT NULL,
    report_id            TEXT         NOT NULL,
    source_ip            TEXT,
    utc_day              DATE,
    message_count        BIGINT,
    spf_result           TEXT,
    dkim_result          TEXT,
    spf_aligned          TEXT,
    dkim_aligned         TEXT,
    envelope_from        TEXT,
    header_from          TEXT,
    dkim_domains         TEXT         NOT NULL,
    disposition          TEXT,
    CONSTRAINT pk_dmarc_aggregate_evidence PRIMARY KEY (id),
    CONSTRAINT fk_dmarc_aggregate_evidence_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_dmarc_aggregate_evidence_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id),
    CONSTRAINT fk_dmarc_aggregate_evidence_receipt
        FOREIGN KEY (receipt_id) REFERENCES dmarc_report_receipt (id)
);

CREATE INDEX idx_dmarc_aggregate_evidence_organization_id ON dmarc_aggregate_evidence (organization_id);
CREATE INDEX idx_dmarc_aggregate_evidence_domain_id ON dmarc_aggregate_evidence (domain_id);
CREATE INDEX idx_dmarc_aggregate_evidence_domain_report ON dmarc_aggregate_evidence (domain_id, report_id);
