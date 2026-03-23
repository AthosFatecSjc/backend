CREATE SCHEMA IF NOT EXISTS energia;

CREATE TABLE IF NOT EXISTS energia.concessionarias (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    codigo_aneel VARCHAR(50) UNIQUE NOT NULL,
    regiao VARCHAR(50),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS energia.indicadores (
    id SERIAL PRIMARY KEY,
    concessionaria_id INTEGER REFERENCES energia.concessionarias(id),
    ano INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    dec_anual DECIMAL(10,2), 
    fec_anual DECIMAL(10,2), 
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(concessionaria_id, ano, mes)
);

CREATE USER energia_app WITH PASSWORD 'app_password';
GRANT CONNECT ON DATABASE energia_db TO energia_app;
GRANT USAGE ON SCHEMA energia TO energia_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA energia TO energia_app;


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
