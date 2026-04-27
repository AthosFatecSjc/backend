ALTER TABLE aneel.distribuidora
ALTER COLUMN razao_social DROP NOT NULL;

ALTER TABLE aneel.distribuidora
ALTER COLUMN regiao DROP NOT NULL;

ALTER TABLE aneel.distribuidora
ALTER COLUMN uf DROP NOT NULL;

ALTER TABLE aneel.distribuidora
ALTER COLUMN contract_type DROP NOT NULL;

ALTER TABLE aneel.conjunto
ALTER COLUMN dsc_conj_und_consumidoras DROP NOT NULL;

ALTER TABLE aneel.metricas
ALTER COLUMN data_geracao_conj_dados DROP NOT NULL;

ALTER TABLE aneel.metricas
ALTER COLUMN vlr_indice_enviado DROP NOT NULL;
