-- =============================================================================
-- MailSent — Flyway migration V12: portfolio.reverse_dns_observation
-- INF-005 (Reverse DNS Observation) production schema. Applied automatically by
-- Flyway at boot, BEFORE Spring Data JPA validates (ddl-auto: validate in the
-- 'it' profile).
--
-- ReverseDnsObservation is an immutable snapshot of one raw reverse-DNS (PTR)
-- observation for an IP of an observed MX host of a registered sending domain
-- (MX hostname → A and/or AAAA → PTR). Tenant binding is the registered
-- Domain's organization_id and domain_id (INV-09). Append-only: there is no
-- UNIQUE on domain_id, so a later observation of the same domain is a new row.
-- ptr_values is TEXT JSON (serialized by the persistence adapter). No FCrDNS,
-- pass/fail, health, score, or diagnosis columns. No JSONB. No triggers.
-- Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS reverse_dns_observation (
    id                   VARCHAR(36)  NOT NULL,
    organization_id      VARCHAR(36)  NOT NULL,
    domain_id            VARCHAR(36)  NOT NULL,
    domain_name          VARCHAR(253) NOT NULL,
    source_mx_hostname   VARCHAR(253) NOT NULL,
    ip                   VARCHAR(45)  NOT NULL,
    address_family       VARCHAR(8)   NOT NULL,
    ptr_values           TEXT         NOT NULL,
    observed_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_reverse_dns_observation PRIMARY KEY (id),
    CONSTRAINT fk_reverse_dns_observation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_reverse_dns_observation_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_reverse_dns_observation_organization_id ON reverse_dns_observation (organization_id);
CREATE INDEX idx_reverse_dns_observation_domain_id ON reverse_dns_observation (domain_id);
