-- =============================================================================
-- MailSent — Flyway migration V11: portfolio.blacklist_observation
-- INF-008 (Blacklist Observation) production schema. Applied automatically by
-- Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in the
-- 'it' profile).
--
-- BlacklistObservation is an immutable snapshot of one raw external blacklist
-- observation (Spamhaus, Barracuda, SURBL, or URIBL) for a registered sending
-- domain. Tenant binding is the registered Domain's organization_id and
-- domain_id (INV-09). Append-only: there is no UNIQUE on domain_id, so a later
-- observation of the same domain is a new row. payload is opaque TEXT. source
-- is SPAMHAUS, BARRACUDA, SURBL, or URIBL. No scores, health, risk, grades, or
-- diagnosis columns. No JSONB. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS blacklist_observation (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    domain_id       VARCHAR(36)  NOT NULL,
    domain_name     VARCHAR(253) NOT NULL,
    source          VARCHAR(32)  NOT NULL,
    observed_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    payload         TEXT         NOT NULL,
    CONSTRAINT pk_blacklist_observation PRIMARY KEY (id),
    CONSTRAINT fk_blacklist_observation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_blacklist_observation_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_blacklist_observation_organization_id ON blacklist_observation (organization_id);
CREATE INDEX idx_blacklist_observation_domain_id ON blacklist_observation (domain_id);
