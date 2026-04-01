DO $$
DECLARE
  v_has_cron BOOLEAN;
BEGIN
  -- verifica se a extensão pg_cron existe
  SELECT EXISTS (
    SELECT 1 FROM pg_extension WHERE extname = 'pg_cron'
  ) INTO v_has_cron;

  IF NOT v_has_cron THEN
    RAISE NOTICE 'pg_cron não instalado, job não será criado';
    RETURN;
  END IF;

  -- remove job existente (se houver)
  PERFORM cron.unschedule(jobid)
  FROM cron.job
  WHERE jobname = 'cleanup_system_logs';

  -- cria job com expurgo em batches (evita lock pesado)
  PERFORM cron.schedule(
    'cleanup_system_logs',
    '0 3 * * *',
    $job$
      DO $inner$
      DECLARE
        v_rows_deleted INT;
      BEGIN
        LOOP
          DELETE FROM system_logs
          WHERE id IN (
            SELECT id
            FROM system_logs
            WHERE created_at < now() - interval '6 months'
            LIMIT 1000
          );

          GET DIAGNOSTICS v_rows_deleted = ROW_COUNT;

          EXIT WHEN v_rows_deleted = 0;

          -- pequeno respiro pra reduzir pressão no banco
          PERFORM pg_sleep(0.1);
        END LOOP;
      END;
      $inner$;
    $job$
  );

EXCEPTION
  WHEN insufficient_privilege THEN
    RAISE NOTICE 'Sem permissão para gerenciar pg_cron';
  WHEN undefined_table THEN
    RAISE NOTICE 'Tabela cron.job não encontrada (pg_cron não ativo)';
END;
$$;