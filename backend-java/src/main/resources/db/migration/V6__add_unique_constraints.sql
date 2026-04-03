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
