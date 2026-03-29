
-- Permitir uso do schema
GRANT USAGE ON SCHEMA aneel TO energia_app;

-- Permitir CRUD nas tabelas
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA aneel TO energia_app;

-- Permitir uso de sequences (BIGSERIAL)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA aneel TO energia_app;


-- =========================
-- TABELA: distribuidora
-- =========================
CREATE TABLE aneel.distribuidora (
    id BIGSERIAL PRIMARY KEY,
    cod_id_dist BIGINT NOT NULL UNIQUE,
    sig_agente VARCHAR(50) NOT NULL UNIQUE,
    num_cnpj VARCHAR(14) NOT NULL UNIQUE,

    regiao VARCHAR(20) NOT NULL,
    contract_type VARCHAR(30) NOT NULL,

    uf VARCHAR(2) NOT NULL,

    CONSTRAINT chk_regiao CHECK (
        regiao IN ('NORTE', 'NORDESTE', 'CENTRO_OESTE', 'SUDESTE', 'SUL')
    ),

    CONSTRAINT chk_contract_type CHECK (
        contract_type IN ('CONCESSIONARIA', 'PERMISSIONARIA')
    )
);


-- =========================
-- TABELA: conjunto
-- =========================
CREATE TABLE aneel.conjunto (
    id BIGSERIAL PRIMARY KEY,
    ide_conj_und_consumidoras BIGINT NOT NULL UNIQUE,
    dsc_conj_und_consumidoras VARCHAR(255) NOT NULL,
    id_distribuidora BIGINT NOT NULL,
    geometry geometry(MultiPolygon, 4674),

    CONSTRAINT fk_conjunto_distribuidora
        FOREIGN KEY (id_distribuidora)
        REFERENCES aneel.distribuidora(id)
);

-- =========================
-- TABELA: limites
-- =========================
CREATE TABLE aneel.limites (
    id BIGSERIAL PRIMARY KEY,
    id_conjunto BIGINT NOT NULL,
    ano BIGINT NOT NULL,
    dec_lim DOUBLE PRECISION NOT NULL,
    fec_lim DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_limites_conjunto
        FOREIGN KEY (id_conjunto)
        REFERENCES aneel.conjunto(id)
);

-- =========================
-- TABELA: sig_indicador
-- =========================
CREATE TABLE aneel.sig_indicador (
    id BIGSERIAL PRIMARY KEY,
    indicador_type VARCHAR(100) NOT NULL,

    CONSTRAINT chk_indicador_type CHECK (
        indicador_type IN ('DEC', 'FEC')
    )
);

-- =========================
-- TABELA: metricas
-- =========================
CREATE TABLE aneel.metricas (
    id BIGSERIAL PRIMARY KEY,
    id_conjunto BIGINT NOT NULL,
    id_sig_indicador BIGINT NOT NULL,
    num_periodo_indice BIGINT NOT NULL,
    ano_indice BIGINT NOT NULL,
    data_geracao_conj_dados DATE NOT NULL,
    vlr_indice_enviado DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_metricas_conjunto
        FOREIGN KEY (id_conjunto)
        REFERENCES aneel.conjunto(id),

    CONSTRAINT fk_metricas_sig_ind
        FOREIGN KEY (id_sig_indicador)
        REFERENCES aneel.sig_indicador(id)
);

-- =========================
-- TABELA: perdas
-- =========================
CREATE TABLE aneel.perdas (
    id BIGSERIAL PRIMARY KEY,
    id_distribuidora BIGINT NOT NULL,
    data_processo DATE,
    tme DOUBLE PRECISION,
    perdas_rede_basica DOUBLE PRECISION,
    custo_perdas_rede_basica DOUBLE PRECISION,
    perdas_tec DOUBLE PRECISION,
    custo_perdas_tec DOUBLE PRECISION,
    perdas_nao_tec DOUBLE PRECISION,
    custo_perdas_nao_tec DOUBLE PRECISION,
    parcela_b DOUBLE PRECISION,
    receita_req DOUBLE PRECISION,

    CONSTRAINT fk_perdas_distribuidora
        FOREIGN KEY (id_distribuidora)
        REFERENCES aneel.distribuidora(id)
);

-- =========================
-- TABELA: subestacao
-- =========================
CREATE TABLE aneel.subestacao (
    id BIGSERIAL PRIMARY KEY,
    cod_id_sub VARCHAR(100) NOT NULL UNIQUE,
    name_sub VARCHAR(255) NOT NULL,
    id_distribuidora BIGINT NOT NULL,
    geometry geometry(MultiPolygon, 4674),

    CONSTRAINT fk_subestacao_distribuidora
        FOREIGN KEY (id_distribuidora)
        REFERENCES aneel.distribuidora(id)
);

ALTER DEFAULT PRIVILEGES IN SCHEMA aneel
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO energia_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA aneel
GRANT USAGE, SELECT ON SEQUENCES TO energia_app;


-- =========================
-- INDICES
-- =========================

CREATE INDEX idx_conjunto_distribuidora 
ON aneel.conjunto(id_distribuidora);

CREATE INDEX idx_limites_conjunto 
ON aneel.limites(id_conjunto);

CREATE INDEX idx_metricas_conjunto 
ON aneel.metricas(id_conjunto);

CREATE INDEX idx_metricas_sig_indicador 
ON aneel.metricas(id_sig_indicador);

CREATE INDEX idx_perdas_distribuidora 
ON aneel.perdas(id_distribuidora);

CREATE INDEX idx_subestacao_distribuidora 
ON aneel.subestacao(id_distribuidora);

-- espaciais (MUITO importante)
CREATE INDEX idx_conjunto_geom 
ON aneel.conjunto USING GIST (geometry);

CREATE INDEX idx_subestacao_geom 
ON aneel.subestacao USING GIST (geometry);