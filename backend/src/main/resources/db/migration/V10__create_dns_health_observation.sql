-- =============================================================================
-- MailSent — Flyway migration V10: portfolio.dns_health_observation
-- INF-007 (DNS Health Observation) production schema. Applied automatically by
-- Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in the
-- 'it' profile).
--
-- DnsHealthObservation is an immutable snapshot of one raw DNS health
-- observation (TTL, DNSSEC-related records, multi-resolver propagation) for a
-- registered sending domain. Tenant binding is the registered Domain's
-- organization_id and domain_id (INV-09). Append-only: there is no UNIQUE on
-- domain_id, so a later observation of the same domain is a new row. TTL,
-- DNSSEC, and propagation payloads are TEXT JSON (serialized by the
-- persistence adapter). No health, score, validity, or diagnosis columns.
-- No JSONB. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS dns_health_observation (
    id                        VARCHAR(36)  NOT NULL,
    organization_id           VARCHAR(36)  NOT NULL,
    domain_id                 VARCHAR(36)  NOT NULL,
    domain_name               VARCHAR(253) NOT NULL,
    observed_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    ttl_observations          TEXT         NOT NULL,
    dnssec_observations       TEXT         NOT NULL,
    propagation_observations  TEXT         NOT NULL,
    CONSTRAINT pk_dns_health_observation PRIMARY KEY (id),
    CONSTRAINT fk_dns_health_observation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_dns_health_observation_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_dns_health_observation_organization_id ON dns_health_observation (organization_id);
CREATE INDEX idx_dns_health_observation_domain_id ON dns_health_observation (domain_id);
