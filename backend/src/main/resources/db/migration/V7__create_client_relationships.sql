-- =============================================================================
-- MailSent — Flyway migration V7: portfolio.client_relationships
-- CLIENT-001 (Agency <=> Client Relationship) production schema.
--
-- A Client Relationship links an Agency Organization to a Client Organization
-- (DOMAIN_MODEL: "an organization that an agency serves"; DATA_MODEL "Client
-- Relationship" aggregate, root Client). An Agency is an Organization acting in
-- an agency capacity; no Agency entity/role is introduced.
--
-- invariants (data model: INV-01, INV-06, INV-09, INV-10, INV-13, INV-14):
--   * the relationship is organization-to-organization only — it references two
--     organizations and never references a User, so it can never become a
--     user-membership mechanism (TEAM-001 frozen); neither organization's
--     identity or lifecycle is altered.
--   * lifecycle status is ACTIVE -> ENDED only; ended_at is NULL while ACTIVE
--     and populated when the relationship ends (irreversible, no physical
--     deletion).
--   * both foreign keys -> organizations(id) guarantee the relationship only
--     references EXISTING organizations (INV-14 no orphans); an organization is
--     never created by CLIENT-001.
--   * one stable identity per relationship (INV-10).
-- The two FKs are the only integrity constraints beyond the PK: no speculative
-- indexes or uniqueness are added (CLIENT-001 contract).
-- Idempotent via IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS client_relationships (
    id                     VARCHAR(36)  NOT NULL,
    agency_organization_id VARCHAR(36)  NOT NULL,
    client_organization_id VARCHAR(36)  NOT NULL,
    formed_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    status                 VARCHAR(16)  NOT NULL,
    ended_at               TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_client_relationships PRIMARY KEY (id),
    CONSTRAINT fk_client_relationships_agency
        FOREIGN KEY (agency_organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_client_relationships_client
        FOREIGN KEY (client_organization_id) REFERENCES organizations (id)
);