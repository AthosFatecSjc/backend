CREATE OR REPLACE PROCEDURE energia.purge_system_logs(
  p_retention_interval INTERVAL DEFAULT '6 months',
  p_batch_size         INT      DEFAULT 1000,
  p_sleep_seconds      NUMERIC  DEFAULT 0.1
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_rows_deleted INT;
BEGIN
  LOOP
    DELETE FROM energia.system_logs
    WHERE id IN (
      SELECT id
      FROM energia.system_logs
      WHERE created_at < now() - p_retention_interval
      LIMIT p_batch_size
    );

    GET DIAGNOSTICS v_rows_deleted = ROW_COUNT;
    EXIT WHEN v_rows_deleted = 0;

    PERFORM pg_sleep(p_sleep_seconds);
  END LOOP;
END;
$$;

DO $$
DECLARE
  v_has_cron BOOLEAN;
BEGIN
  SELECT EXISTS (
    SELECT 1 FROM pg_extension WHERE extname = 'pg_cron'
  ) INTO v_has_cron;

  IF NOT v_has_cron THEN
    -- Retenção não fica bloqueada: a procedure existe e pode
    -- ser invocada por scheduler externo ou manualmente.
    RAISE WARNING
      'pg_cron não instalado. '
      'Execute manualmente quando necessário: '
      'CALL energia.purge_system_logs();';
    RETURN;
  END IF;

  -- Remove job anterior para evitar duplicatas
  PERFORM cron.unschedule(jobid)
  FROM cron.job
  WHERE jobname = 'cleanup_system_logs';

  -- Registra o job delegando para a procedure
  PERFORM cron.schedule(
    'cleanup_system_logs',
    '0 3 * * *',
    $job$CALL energia.purge_system_logs();$job$
  );

  RAISE NOTICE 'Job cleanup_system_logs registrado no pg_cron (diário às 03h00).';

EXCEPTION
  WHEN insufficient_privilege THEN
    RAISE WARNING
      'Sem permissão para gerenciar pg_cron. '
      'Execute manualmente: CALL energia.purge_system_logs();';
  WHEN undefined_table THEN
    RAISE WARNING
      'Tabela cron.job não encontrada (pg_cron inativo). '
      'Execute manualmente: CALL energia.purge_system_logs();';
END;
$$;