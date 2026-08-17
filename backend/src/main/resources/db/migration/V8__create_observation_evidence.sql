-- =============================================================================
-- MailSent — Flyway migration V8: portfolio.observation_evidence
-- INF-002 (Observation Evidence) production schema. Applied automatically by
-- Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in the
-- 'it' profile).
--
-- ObservationEvidence is an immutable snapshot of one real DNS observation for a
-- registered sending domain. Tenant binding is the registered Domain's
-- organization_id and domain_id (INV-09). Append-only: there is no UNIQUE on
-- domain_id, so a later observation of the same domain is a new row. MX and TXT
-- are TEXT JSON arrays (serialized by the persistence adapter). spf_record is
-- nullable. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS observation_evidence (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    domain_id       VARCHAR(36)  NOT NULL,
    domain_name     VARCHAR(253) NOT NULL,
    observed_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    mx_records      TEXT         NOT NULL,
    txt_records     TEXT         NOT NULL,
    spf_record      TEXT,
    CONSTRAINT pk_observation_evidence PRIMARY KEY (id),
    CONSTRAINT fk_observation_evidence_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_observation_evidence_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_observation_evidence_organization_id ON observation_evidence (organization_id);
CREATE INDEX idx_observation_evidence_domain_id ON observation_evidence (domain_id);
