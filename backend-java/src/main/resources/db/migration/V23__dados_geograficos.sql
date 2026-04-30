CREATE INDEX IF NOT EXISTS idx_conjunto_geometry
ON aneel.conjunto
USING GIST (geometry);