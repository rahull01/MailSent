-- =============================================================================
-- MailSent — Flyway migration V6: identity.team invitations + memberships
-- TEAM-001 (invitation + organization-membership foundation) production schema.
-- Applied automatically by Flyway at boot, BEFORE Spring Data JPA validates
-- (ddl-auto: validate in the 'it' profile).
--
-- A User keeps exactly one immutable home Organization (User.organization_id,
-- ORG-002 / D6 — untouched). TEAM membership is a SEPARATE concept: a User may
-- hold zero or more explicit Memberships in other Organizations.
--
-- invitations:
--   * binds an invitee email to a target Organization with a granted role;
--   * token_hash stores ONLY the SHA-256 of the acceptance token (never the raw
--     bearer value) and is UNIQUE, so a leaked token is not recoverable and a
--     token resolves to at most one invitation (single-use backstop);
--   * status PENDING -> ACCEPTED enforces single-use acceptance;
--   * the ownership FK (organization_id -> organizations) guarantees a membership
--     is never created for a non-existent Organization (INV-14).
--
-- memberships:
--   * UNIQUE(user_id, organization_id) is the race-safe backstop preventing
--     duplicate active membership for a (user, organization) pair;
--   * FKs guarantee a membership only references an existing User and an existing
--     Organization (INV-14);
--   * accepting an invitation never mutates the User's home organization.
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
    CONSTRAINT pk_invitations             PRIMARY KEY (id),
    CONSTRAINT uk_invitations_token_hash  UNIQUE (token_hash),
    CONSTRAINT fk_invitations_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

CREATE INDEX idx_invitations_organization_id ON invitations (organization_id);

CREATE TABLE IF NOT EXISTS memberships (
    id              VARCHAR(36) NOT NULL,
    user_id         VARCHAR(36) NOT NULL,
    organization_id VARCHAR(36) NOT NULL,
    role            VARCHAR(16) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_memberships                    PRIMARY KEY (id),
    CONSTRAINT uk_memberships_user_organization  UNIQUE (user_id, organization_id),
    CONSTRAINT fk_memberships_user               FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_memberships_organization       FOREIGN KEY (organization_id) REFERENCES organizations (id)
);