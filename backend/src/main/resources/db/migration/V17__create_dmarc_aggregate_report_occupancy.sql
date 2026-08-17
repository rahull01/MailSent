-- =============================================================================
-- MailSent — Flyway migration V17: dmarc_aggregate_report_occupancy
-- INT-005 concurrency guard. Applied automatically by Flyway at boot, BEFORE
-- Spring Data JPA validates (ddl-auto: validate in the 'it' profile).
--
-- One occupancy row means this registered Domain already has a stored
-- aggregate snapshot set for this report_id. Multiple source_ip rows remain
-- on dmarc_aggregate_evidence (V15); that table must NOT be UNIQUE on
-- (domain_id, report_id). Occupancy is the sole uniqueness boundary:
-- (domain_id, report_id). Tenant identity is domain_id (INV-09); report_id
-- alone is never unique. Append-only: occupancy is insert-only. V1–V16 are
-- not altered.
-- =============================================================================

CREATE TABLE IF NOT EXISTS dmarc_aggregate_report_occupancy (
    domain_id       VARCHAR(36)  NOT NULL,
    report_id       TEXT         NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    CONSTRAINT pk_dmarc_aggregate_report_occupancy PRIMARY KEY (domain_id, report_id),
    CONSTRAINT fk_dmarc_aggregate_report_occupancy_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id),
    CONSTRAINT fk_dmarc_aggregate_report_occupancy_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
