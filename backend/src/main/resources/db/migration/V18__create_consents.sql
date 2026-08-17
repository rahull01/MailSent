CREATE TABLE consents (
    id VARCHAR(36) PRIMARY KEY,
    organization_id VARCHAR(200) NOT NULL,
    domain_id VARCHAR(200) NOT NULL,
    granted_by_user_id VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_consents_active ON consents (domain_id, action_type) WHERE status = 'GRANTED';
