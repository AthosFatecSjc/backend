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
