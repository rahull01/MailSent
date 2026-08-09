-- =============================================================================
-- MailSent — Flyway migration V4: identity.users → organization membership
-- ORG-002 (User ↔ Organization lifecycle) production schema. Applied at boot
-- BEFORE Spring Data JPA validates (ddl-auto: validate in the 'it' profile).
--
-- Purpose: bind a User to exactly one home Organization (D6 / "a user belongs
-- to exactly one organization"; INV-09). This is ADDITIVE and NON-DESTRUCTIVE:
--   * a nullable organization_id column is added — safe against any pre-existing
--     rows, which are left untouched (no backfill, no fake/default orgs, no random
--     mapping, no DELETE);
--   * a foreign key enforces referential integrity (a User can never reference a
--     non-existent Organization, so no dangling membership);
--   * an index supports the canonical tenant-scoped lookup (organization_id).
--
-- New-signup invariant: the application layer constructs every new User with its
-- Organization id inside one atomic transaction (see SignUpService), so a new
-- User is NEVER persisted with a missing organization. The column is intentionally
-- NULLABLE here purely for migration compatibility; tightening it to NOT NULL is a
-- separate, governed follow-up that must first resolve whether any pre-existing
-- users (currently none at product launch) carry a NULL before applying.
-- =============================================================================

ALTER TABLE users ADD COLUMN organization_id VARCHAR(36);

ALTER TABLE users
    ADD CONSTRAINT fk_users_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE INDEX idx_users_organization_id ON users (organization_id);