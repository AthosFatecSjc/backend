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