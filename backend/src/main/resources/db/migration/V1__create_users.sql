-- =============================================================================
-- MailSent — Flyway migration V1: identity.users
-- AUTH-000 (Sign Up) production schema. Applied automatically by Flyway at
-- application boot BEFORE Spring Data JPA validates against it (ddl-auto: validate
-- in production), so no manual SQL steps are required for deployment.
--
-- Idempotency: Flyway records this version in flyway_schema_history and applies it
-- exactly once per schema; IF NOT EXISTS guards a pre-existing table. Column widths
-- and the unique-constraint name match the UserJpaEntity mapping so JPA validate
-- passes unchanged.
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(36)  NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    role          VARCHAR(16)  NOT NULL,
    -- java.time.Instant maps to timestamp-with-time-zone on PostgreSQL; this matches
    -- Hibernate's expected type so ddl-auto: validate passes.
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_users       PRIMARY KEY (id),
    -- Email is the canonical, case-folded identity of a User (INV-10). The unique
    -- constraint is both a data-integrity guard and the race-safe backstop the
    -- sign-up use case relies on for duplicate detection. Name matches the JPA
    -- mapping (uk_users_email) so ddl-auto: validate accepts it.
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Time-ordered scans (registration history / reporting) use created_at.
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at);