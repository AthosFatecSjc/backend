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