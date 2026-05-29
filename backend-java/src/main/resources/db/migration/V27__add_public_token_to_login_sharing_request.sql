ALTER TABLE login_sharing_request
ADD COLUMN public_token VARCHAR(64),
ADD COLUMN public_token_expires_at TIMESTAMP;

CREATE UNIQUE INDEX idx_login_sharing_public_token
ON login_sharing_request(public_token);

CREATE INDEX idx_login_sharing_public_token_expires_at
ON login_sharing_request(public_token_expires_at);
