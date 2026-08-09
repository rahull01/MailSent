-- =============================================================================
-- MailSent — Flyway migration V5: portfolio.domains
-- ORG-003 (domain ownership) production schema. Applied automatically by Flyway at
-- boot, BEFORE Spring Data JPA validates (ddl-auto: validate in the 'it' profile).
--
-- A real-world sending domain belongs to exactly one Organization (INV-09). The
-- owning organization is NOT NULL with a foreign key to organizations, so a domain
-- can never exist without a (real) owner — the application service checks org
-- existence for a clear domain error, and this FK is the final integrity backstop.
-- A domain name is globally unique across all organizations; the UNIQUE constraint
-- (uk_domains_name) is both a data-integrity guard and the race-safe backstop the
-- register use case relies on for duplicate detection. Name matches the JPA mapping
-- so ddl-auto: validate passes. Idempotent via IF NOT EXISTS; the org-board index
-- supports the canonical owner-scoped lookup.
-- =============================================================================

CREATE TABLE IF NOT EXISTS domains (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    name            VARCHAR(253) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_domains            PRIMARY KEY (id),
    CONSTRAINT uk_domains_name       UNIQUE (name),
    CONSTRAINT fk_domains_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

CREATE INDEX idx_domains_organization_id ON domains (organization_id);