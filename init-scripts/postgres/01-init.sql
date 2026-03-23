CREATE SCHEMA IF NOT EXISTS energia;

-- Create users
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'energia_app') THEN
      CREATE USER energia_app WITH PASSWORD 'app_password';
   END IF;
END
$$;

DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'flyway_user') THEN
      CREATE USER flyway_user WITH PASSWORD 'flyway_password';
   END IF;
END
$$;

-- [ENERGIA_APP] Schema access
GRANT USAGE ON SCHEMA energia TO energia_app;

-- [ENERGIA_APP] CRUD
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA energia TO energia_app;

-- [ENERGIA_APP] Sequences
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA energia TO energia_app;

-- [ENERGIA_APP] Future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA energia
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO energia_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA energia
GRANT USAGE, SELECT ON SEQUENCES TO energia_app;

-- [FLYWAY_USER] Schema access
GRANT USAGE ON SCHEMA energia TO flyway_user;
GRANT CREATE ON SCHEMA energia TO flyway_user;

-- [FLYWAY_USER] Existing objects
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA energia TO flyway_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA energia TO flyway_user;

-- [FLYWAY_USER] Future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA energia
GRANT ALL ON TABLES TO flyway_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA energia
GRANT ALL ON SEQUENCES TO flyway_user;

-- [FLYWAY_USER] Ownership
ALTER SCHEMA energia OWNER TO flyway_user;


-- Logs
CREATE TABLE energia.system_logs (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_ref VARCHAR(100),
    source_type VARCHAR(20) NOT NULL,
    event VARCHAR(50) NOT NULL,
    result VARCHAR(10) NOT NULL,
    log_category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    metadata TEXT,
    target_ref VARCHAR(100),
    created_by_module VARCHAR(100)
);

CREATE INDEX idx_logs_created_at 
ON energia.system_logs (created_at);

CREATE INDEX idx_logs_source_type 
ON energia.system_logs (source_type);

CREATE INDEX idx_logs_event 
ON energia.system_logs (event);

CREATE INDEX idx_logs_actor 
ON energia.system_logs (actor_ref);

CREATE INDEX idx_logs_result 
ON energia.system_logs (result);

CREATE INDEX idx_logs_event_created_at 
ON energia.system_logs (event, created_at);

CREATE INDEX idx_logs_failures 
ON energia.system_logs (event)
WHERE result = 'FAIL';

ALTER TABLE energia.system_logs
ADD CONSTRAINT chk_result
CHECK (result IN ('SUCCESS', 'FAIL'));
