-- =============================================================================
-- MailSent — Flyway migration V3: portfolio.organizations
-- ORG-001 (Phase 03) production schema. Applied automatically by Flyway at boot,
-- BEFORE Spring Data JPA validates (ddl-auto: validate in prod).
--
-- The organization is the ownership/isolation boundary (INV-09). Only the stable
-- identity, name, formation time, and lifecycle state (ACTIVE/CLOSED + closed_at) are
-- stored. No membership, ownership, domain, or consent tables belong to ORG-001.
-- Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS organizations (
    id         VARCHAR(36) NOT NULL,
    name       VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status     VARCHAR(16) NOT NULL,
    closed_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_organizations PRIMARY KEY (id)
);