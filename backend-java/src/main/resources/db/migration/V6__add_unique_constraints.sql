ALTER TABLE aneel.limites
ADD CONSTRAINT uk_limites UNIQUE (id_conjunto, ano);

ALTER TABLE aneel.metricas
ADD CONSTRAINT uk_metricas UNIQUE (
    id_conjunto,
    id_sig_indicador,
    num_periodo_indice,
    ano_indice
);

ALTER TABLE aneel.perdas ADD COLUMN ano BIGINT;

ALTER TABLE aneel.perdas
ADD CONSTRAINT uk_perdas UNIQUE (id_distribuidora, ano);

ALTER TABLE aneel.distribuidora
ADD COLUMN razao_social VARCHAR(255) NOT NULL;

ALTER TABLE aneel.distribuidora
DROP CONSTRAINT chk_contract_type;

-- 2. Cria novamente com o novo valor
ALTER TABLE aneel.distribuidora
ADD CONSTRAINT chk_contract_type CHECK (
    contract_type IN ('CONCESSIONARIA', 'PERMISSIONARIA', 'DESIGNADA')
);

CREATE TABLE aneel.coleta (
    id BIGSERIAL PRIMARY KEY,

    data_key VARCHAR(50) NOT NULL,

    id_data BIGINT NOT NULL,

    data_coleta DATE NOT NULL,

    data_geracao DATE,

    link TEXT NOT NULL
);

ALTER TABLE aneel.sig_indicador
ADD CONSTRAINT uk_sig_indicador UNIQUE (indicador_type);


INSERT INTO aneel.sig_indicador (indicador_type)
SELECT 'DEC'
WHERE NOT EXISTS (
    SELECT 1 FROM aneel.sig_indicador WHERE indicador_type = 'DEC'
);

INSERT INTO aneel.sig_indicador (indicador_type)
SELECT 'FEC'
WHERE NOT EXISTS (
    SELECT 1 FROM aneel.sig_indicador WHERE indicador_type = 'FEC'
);