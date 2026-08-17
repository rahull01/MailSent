-- =============================================================================
-- MailSent — Flyway migration V13: portfolio.smtp_greeting_observation
-- INF-006 (SMTP Greeting Observation) production schema. Applied automatically
-- by Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in
-- the 'it' profile).
--
-- SmtpGreetingObservation is an immutable snapshot of one raw SMTP
-- greeting/banner and EHLO observation for an IP of an observed MX host of a
-- registered sending domain (MX hostname from Observation Evidence → A and/or
-- AAAA → SMTP port 25). Tenant binding is the registered Domain's
-- organization_id and domain_id (INV-09). Append-only: there is no UNIQUE on
-- domain_id, so a later observation of the same domain is a new row.
-- raw_greeting and raw_ehlo are TEXT (nullable when not observed). probe_outcome
-- is VARCHAR (CONNECTED / UNAVAILABLE / REFUSED / TIMEOUT) — raw connection
-- fact, not health. No JSONB. No triggers. Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS smtp_greeting_observation (
    id                   VARCHAR(36)  NOT NULL,
    organization_id      VARCHAR(36)  NOT NULL,
    domain_id            VARCHAR(36)  NOT NULL,
    domain_name          VARCHAR(253) NOT NULL,
    source_mx_hostname   VARCHAR(253) NOT NULL,
    ip                   VARCHAR(45)  NOT NULL,
    address_family       VARCHAR(8)   NOT NULL,
    probe_outcome        VARCHAR(16)  NOT NULL,
    raw_greeting         TEXT,
    raw_ehlo             TEXT,
    observed_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_smtp_greeting_observation PRIMARY KEY (id),
    CONSTRAINT fk_smtp_greeting_observation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_smtp_greeting_observation_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_smtp_greeting_observation_organization_id ON smtp_greeting_observation (organization_id);
CREATE INDEX idx_smtp_greeting_observation_domain_id ON smtp_greeting_observation (domain_id);
