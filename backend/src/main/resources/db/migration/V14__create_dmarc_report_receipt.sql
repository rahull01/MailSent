-- =============================================================================
-- MailSent — Flyway migration V14: dmarc_report_receipt
-- INT-003 (DMARC Report Receive) production schema. Applied automatically
-- by Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in
-- the 'it' profile).
--
-- DMARC Report Receipt is an immutable opaque payload received for a
-- registered Domain via the RUA ingest webhook. Tenant binding is the
-- registered Domain's organization_id and domain_id (INV-09). Append-only:
-- there is no UNIQUE on domain_id, so a later receipt of the same domain is
-- a new row. report_id is nullable TEXT: INT-003 must not parse XML to fill
-- it. Duplicate report_id + Domain idempotency is INT-004/005 once parse
-- succeeds. No UNIQUE on payload. No parsed/aggregate columns. No JSONB.
-- No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS dmarc_report_receipt (
    id                   VARCHAR(36)  NOT NULL,
    organization_id      VARCHAR(36)  NOT NULL,
    domain_id            VARCHAR(36)  NOT NULL,
    domain_name          VARCHAR(253) NOT NULL,
    report_id            TEXT,
    payload              BYTEA        NOT NULL,
    received_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_dmarc_report_receipt PRIMARY KEY (id),
    CONSTRAINT fk_dmarc_report_receipt_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_dmarc_report_receipt_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_dmarc_report_receipt_organization_id ON dmarc_report_receipt (organization_id);
CREATE INDEX idx_dmarc_report_receipt_domain_id ON dmarc_report_receipt (domain_id);
