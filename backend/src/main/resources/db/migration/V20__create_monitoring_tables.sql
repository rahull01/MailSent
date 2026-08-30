-- =============================================================================
-- MailSent — Flyway migration V20: monitoring.incidents and health_change_events
-- MON-001 / MON-002 (Monitoring & Alerting Bounded Context) production schema.
-- Applied automatically by Flyway at boot, BEFORE Spring Data JPA validates
-- (ddl-auto: validate in the 'it' and 'prod' profiles).
--
-- Tenant binding is strictly organization_id and domain_id (INV-09, INV-13).
-- Foreign keys reference organizations(id) and domains(id).
-- Active incident uniqueness: at most ONE active incident per (organization_id, domain_id)
-- where resolved_at IS NULL.
-- =============================================================================

CREATE TABLE IF NOT EXISTS incidents (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    domain_id       VARCHAR(36)  NOT NULL,
    opened_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at     TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_incidents PRIMARY KEY (id),
    CONSTRAINT fk_incidents_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_incidents_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_incidents_domain_id ON incidents (domain_id);
CREATE INDEX idx_incidents_org_domain ON incidents (organization_id, domain_id);

CREATE UNIQUE INDEX uk_incidents_active 
ON incidents (organization_id, domain_id) 
WHERE resolved_at IS NULL;

CREATE TABLE IF NOT EXISTS health_change_events (
    id                  VARCHAR(36)  NOT NULL,
    organization_id     VARCHAR(36)  NOT NULL,
    domain_id           VARCHAR(36)  NOT NULL,
    previous_indication VARCHAR(64),
    current_indication  VARCHAR(64)  NOT NULL,
    detected_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_health_change_events PRIMARY KEY (id),
    CONSTRAINT fk_health_change_events_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_health_change_events_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_health_change_events_org_domain_detected 
ON health_change_events (organization_id, domain_id, detected_at DESC);
