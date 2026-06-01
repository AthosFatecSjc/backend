-- V26: Create login_sharing_request table for OAuth-like testing

CREATE TABLE login_sharing_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_agent_name VARCHAR(255) NOT NULL,
    external_agent_email VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    user_response_code VARCHAR(255),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_sharing_user_id ON login_sharing_request(user_id);
CREATE INDEX idx_login_sharing_status ON login_sharing_request(status);
CREATE INDEX idx_login_sharing_expires_at ON login_sharing_request(expires_at);

-- Grant privileges to energia_app role
GRANT SELECT, INSERT, UPDATE ON login_sharing_request TO energia_app;
