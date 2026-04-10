DO $$
DECLARE
    ativo_id UUID;
BEGIN
    SELECT id
    INTO ativo_id
    FROM status
    WHERE UPPER(TRIM(name)) = 'ATIVO'
    LIMIT 1;

    IF ativo_id IS NULL THEN
        INSERT INTO status (id, name)
        VALUES (gen_random_uuid(), 'ATIVO')
        RETURNING id INTO ativo_id;
    END IF;

    UPDATE user_status
    SET status_id = ativo_id
    WHERE status_id IN (
        SELECT id
        FROM status
        WHERE UPPER(TRIM(name)) = 'APROVADO'
    );
END $$;

DELETE FROM status
WHERE UPPER(TRIM(name)) = 'APROVADO';

ALTER TABLE status
    DROP CONSTRAINT IF EXISTS chk_status_name_official_lifecycle;

ALTER TABLE status
    ADD CONSTRAINT chk_status_name_official_lifecycle
    CHECK (UPPER(TRIM(name)) IN ('PENDENTE', 'ATIVO', 'REJEITADO'));
