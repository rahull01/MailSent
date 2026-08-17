-- =============================================================================
-- MailSent — Flyway migration V9: portfolio.reputation_observation
-- INF-003 (Reputation Ingest) production schema. Applied automatically by
-- Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in the
-- 'it' profile).
--
-- ReputationObservation is an immutable snapshot of one raw external reputation
-- observation (Google Postmaster or Microsoft SNDS) for a registered sending
-- domain. Tenant binding is the registered Domain's organization_id and
-- domain_id (INV-09). Append-only: there is no UNIQUE on domain_id, so a later
-- observation of the same domain is a new row. payload is opaque TEXT. source
-- is POSTMASTER or SNDS. No scores, health, risk, grades, or diagnosis
-- columns. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS reputation_observation (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    domain_id       VARCHAR(36)  NOT NULL,
    domain_name     VARCHAR(253) NOT NULL,
    source          VARCHAR(32)  NOT NULL,
    observed_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    payload         TEXT         NOT NULL,
    CONSTRAINT pk_reputation_observation PRIMARY KEY (id),
    CONSTRAINT fk_reputation_observation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_reputation_observation_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_reputation_observation_organization_id ON reputation_observation (organization_id);
CREATE INDEX idx_reputation_observation_domain_id ON reputation_observation (domain_id);
