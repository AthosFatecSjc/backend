ALTER TABLE aneel.perdas
DROP COLUMN IF EXISTS perdas_rede_basica,
DROP COLUMN IF EXISTS custo_perdas_rede_basica,
DROP COLUMN IF EXISTS perdas_tec,
DROP COLUMN IF EXISTS custo_perdas_tec,
DROP COLUMN IF EXISTS parcela_b,
DROP COLUMN IF EXISTS receita_req;
