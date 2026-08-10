-- =============================================================================
-- MailSent — Flyway migration V6: identity.team invitations
-- TEAM-001 (invitation + secure single-use acceptance) production schema,
-- per the frozen v8 contract §6.
--
-- The migration creates ONLY the invitations persistence required by TEAM-001.
-- It intentionally creates NO memberships table, NO relationship/scope tables,
-- and NO RBAC tables. A User keeps exactly one immutable home Organization
-- (User.organization_id, ORG-002 / D6 — untouched); acceptance creates no
-- membership and does not expand organizational scope.
--
-- invariants:
--   * status permits only PENDING / ACCEPTED; PENDING -> ACCEPTED only.
--   * accepted_at is NULL while PENDING and populated when PENDING -> ACCEPTED
--     succeeds.
--   * token_hash stores ONLY the SHA-256 of the raw acceptance token (never the
--     raw bearer value) and is UNIQUE — this establishes TOKEN IDENTITY only
--     (it is NOT the single-use/concurrency mechanism).
--   * single-use is enforced by an atomic conditional transition
--     (UPDATE ... WHERE id=? AND status='PENDING') in the application, NOT by a
--     database table trigger.
--   * the PENDING-scoped partial unique index on (organization_id, invitee_email)
--     WHERE status='PENDING' forbids duplicate pending invitations race-safely.
--   * the ownership FK (organization_id -> organizations) guarantees an
--     invitation never references a non-existent Organization (INV-14).
--   * invitee_email is persisted in the existing approved Email canonical form;
--     the database introduces no additional email normalization (no CITEXT, no
--     case-folding, no lowercasing trigger, no collation equivalence).
-- Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS invitations (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    invitee_email   VARCHAR(254) NOT NULL,
    role            VARCHAR(16)  NOT NULL,
    token_hash      VARCHAR(64)  NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at     TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_invitations             PRIMARY KEY (id),
    CONSTRAINT uk_invitations_token_hash  UNIQUE (token_hash),
    CONSTRAINT fk_invitations_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

CREATE INDEX idx_invitations_organization_id ON invitations (organization_id);

-- Race-safe duplicate-PENDING backstop: at most one PENDING invitation for the
-- exact pair (organization_id, canonical invitee_email). ACCEPTED invitations
-- are excluded from the uniqueness window so a later re-invitation is possible.
CREATE UNIQUE INDEX uk_invitations_pending_org_email
    ON invitations (organization_id, invitee_email)
    WHERE status = 'PENDING';