-- =============================================================================
-- MailSent — Flyway migration V2: identity.sessions
-- AUTH-001 (Sign in / session) production schema. Applied automatically by Flyway at
-- application boot BEFORE Spring Data JPA validates (ddl-auto: validate in prod).
--
-- A session stores only the hash of its bearer token — never the raw value — so a
-- database leak cannot disclose a usable credential. The unique constraint on the hash
-- (uk_sessions_token_hash) keys each session and guards against duplicate issues.
-- =============================================================================

CREATE TABLE IF NOT EXISTS sessions (
    id         VARCHAR(36) NOT NULL,
    user_id    VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_sessions PRIMARY KEY (id),
    CONSTRAINT uk_sessions_token_hash UNIQUE (token_hash)
);

-- The uk_sessions_token_hash unique constraint doubles as the lookup index for
-- verification; a second index on the same column would be redundant.