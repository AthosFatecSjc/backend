-- Create application user role if it doesn't exist
DO $$
BEGIN
  CREATE ROLE energia_app WITH LOGIN PASSWORD 'app_password';
EXCEPTION
  WHEN duplicate_object THEN
    NULL;
END
$$;

-- Grant schema usage
GRANT USAGE ON SCHEMA energia TO energia_app;

-- Grant table permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA energia TO energia_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA energia TO energia_app;

-- Grant default privileges for future objects
ALTER DEFAULT PRIVILEGES FOR USER flyway_user IN SCHEMA energia
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO energia_app;

ALTER DEFAULT PRIVILEGES FOR USER flyway_user IN SCHEMA energia
GRANT USAGE, SELECT ON SEQUENCES TO energia_app;
