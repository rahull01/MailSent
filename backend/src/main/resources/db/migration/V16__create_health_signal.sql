-- =============================================================================
-- MailSent — Flyway migration V16: health_signal
-- HEALTH-001 (Health Signal envelope) production schema. Applied automatically
-- by Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in
-- the 'it' profile).
--
-- Health Signal is an immutable unified indication snapshot for a registered
-- Domain (INV-05 current = latest by observed_at). Tenant binding is the
-- registered Domain's organization_id and domain_id (INV-09). Append-only:
-- there is no UNIQUE on domain_id, so a later derivation is a new row.
-- indication is TEXT (healthy | at-risk | unknown). Lineage cites Observation
-- Evidence, DMARC Aggregate Evidence ids actually used, and Reputation
-- Observation ids actually used (INV-11). Aggregate and reputation id lists
-- are TEXT JSON arrays (OE style). No Trust Score, MAP color, trend, Issue,
-- Alignment, or DIAG-001 columns. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS health_signal (
    id                              VARCHAR(36)  NOT NULL,
    organization_id                 VARCHAR(36)  NOT NULL,
    domain_id                       VARCHAR(36)  NOT NULL,
    domain_name                     VARCHAR(253) NOT NULL,
    indication                      TEXT         NOT NULL,
    observation_evidence_id         VARCHAR(36)  NOT NULL,
    dmarc_aggregate_evidence_ids    TEXT         NOT NULL,
    reputation_observation_ids      TEXT         NOT NULL,
    observed_at                     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_health_signal PRIMARY KEY (id),
    CONSTRAINT fk_health_signal_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_health_signal_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_health_signal_organization_id ON health_signal (organization_id);
CREATE INDEX idx_health_signal_domain_id ON health_signal (domain_id);
